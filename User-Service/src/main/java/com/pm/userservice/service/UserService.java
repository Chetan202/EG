package com.pm.userservice.service;

import com.pm.userservice.dto.*;
import com.pm.userservice.entity.Enterprise;
import com.pm.userservice.entity.User;
import com.pm.userservice.enums.UserRole;
import com.pm.userservice.repository.EnterpriseRepository;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service - Production level with multi-tenancy and authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PermissionService permissionService;

    /**
     * User login with manual authentication
     */
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {} in enterprise: {}", loginRequest.getEmail(), loginRequest.getEnterpriseId());

        // Verify enterprise exists
        Enterprise enterprise = enterpriseRepository.findById(loginRequest.getEnterpriseId())
                .orElseThrow(() -> new IllegalArgumentException("Enterprise not found: " + loginRequest.getEnterpriseId()));

        // Find user in the enterprise
        User user = userRepository.findByEmailAndEnterpriseId(loginRequest.getEmail(), loginRequest.getEnterpriseId())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Login attempt for locked account: {}", user.getEmail());
            throw new BadCredentialsException("Account is locked. Please try again later.");
        }

        // Check if user is active and verified
        if (!user.isEnabled()) {
            throw new BadCredentialsException("User account is not active or email not verified");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.lockAccount();
                userRepository.save(user);
                throw new BadCredentialsException("Account locked due to multiple failed login attempts");
            }

            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        // Reset failed attempts and update last login
        user.setFailedLoginAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        user.unlockAccount();
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(user, enterprise.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), enterprise.getId());

        log.info("User logged in successfully: {}", user.getEmail());

        return buildAuthResponse(user, enterprise, accessToken, refreshToken);
    }

    /**
     * Create a new user with role-based permission checks
     * CEO can only create HR users
     * HR can only create EMPLOYEE users
     * SUPER_ADMIN can create any role
     */
    public UserDto createUser(UserCreateRequest request, User creator) {
        log.info("Creating new user: {} in enterprise: {} by {}",
                request.getEmail(), request.getEnterpriseId(), creator.getEmail());

        // Check if creator has permission to create this role
        if (!permissionService.canCreateUser(creator, request.getRole(), request.getEnterpriseId())) {
            throw new IllegalArgumentException(
                    "You do not have permission to create user with role: " + request.getRole().getCode());
        }

        // Verify enterprise exists
        Enterprise enterprise = enterpriseRepository.findById(request.getEnterpriseId())
                .orElseThrow(() -> new IllegalArgumentException("Enterprise not found: " + request.getEnterpriseId()));

        // Check if email already exists in enterprise
        if (userRepository.existsByEmailAndEnterpriseId(request.getEmail(), request.getEnterpriseId())) {
            throw new IllegalArgumentException("Email already exists in this enterprise");
        }

        // Check if employee ID already exists in enterprise
        if (userRepository.existsByEmployeeIdAndEnterpriseId(request.getEmployeeId(), request.getEnterpriseId())) {
            throw new IllegalArgumentException("Employee ID already exists in this enterprise");
        }

        // Build user entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeId(request.getEmployeeId())
                .enterprise(enterprise)
                .role(request.getRole() != null ? request.getRole() : UserRole.EMPLOYEE)
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .emailVerified(false)
                .build();

        // Set manager if provided
        if (request.getManagerId() != null && !request.getManagerId().isEmpty()) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

            // Validate manager assignment
            if (!permissionService.canAssignManager(creator, user, manager)) {
                throw new IllegalArgumentException("You cannot assign this user as manager");
            }
            user.setManager(manager);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {} with role: {}", savedUser.getEmail(), savedUser.getRole().getCode());

        return mapToUserDto(savedUser);
    }

    /**
     * Overload for backward compatibility
     */
    public UserDto createUser(UserCreateRequest request) {
        // This should be called with creator context from controller
        throw new IllegalArgumentException("Creator user context is required");
    }

    /**
     * Get user by ID
     */
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return mapToUserDto(user);
    }

    /**
     * Get user by email in enterprise
     */
    public UserDto getUserByEmailInEnterprise(String email, String enterpriseId) {
        User user = userRepository.findByEmailAndEnterpriseId(email, enterpriseId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToUserDto(user);
    }

    /**
     * Get all users in enterprise
     */
    public List<UserDto> getAllUsersInEnterprise(String enterpriseId) {
        List<User> users = userRepository.findByEnterpriseIdAndActiveTrue(enterpriseId);
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role in enterprise
     */
    public List<UserDto> getUsersByRoleInEnterprise(String enterpriseId, UserRole role) {
        List<User> users = userRepository.findByEnterpriseIdAndRole(enterpriseId, role);
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all HR users in enterprise
     */
    public List<UserDto> getHRUsersInEnterprise(String enterpriseId) {
        List<User> users = userRepository.findHRUsersInEnterprise(enterpriseId);
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Get reports of a manager
     */
    public List<UserDto> getManagerReports(String managerId, String enterpriseId) {
        List<User> reports = userRepository.findByManagerIdAndEnterpriseId(managerId, enterpriseId);
        return reports.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Update user
     */
    public UserDto updateUser(String userId, UserDto updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getDepartment() != null) {
            user.setDepartment(updateRequest.getDepartment());
        }
        if (updateRequest.getDesignation() != null) {
            user.setDesignation(updateRequest.getDesignation());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getProfileImageUrl() != null) {
            user.setProfileImageUrl(updateRequest.getProfileImageUrl());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserDto(updatedUser);
    }

    /**
     * Deactivate user with permission check
     */
    public void deactivateUser(String userId, User actor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if actor has permission to deactivate this user
        if (!permissionService.canDeactivateUser(actor, user)) {
            throw new IllegalArgumentException(
                    "You do not have permission to deactivate this user");
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {} by {}", user.getEmail(), actor.getEmail());
    }

    /**
     * Overload for backward compatibility
     */
    public void deactivateUser(String userId) {
        throw new IllegalArgumentException("Actor user context is required");
    }

    /**
     * Verify user email
     */
    public void verifyUserEmail(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailVerified(true);
        user.setEmailVerificationDate(LocalDateTime.now());
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    /**
     * Change password
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChangeDate(LocalDateTime.now());
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    /**
     * Helper: Map User entity to UserDto
     */
    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .employeeId(user.getEmployeeId())
                .role(user.getRole())
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .phoneNumber(user.getPhoneNumber())
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .enterpriseId(user.getEnterprise().getId())
                .enterpriseName(user.getEnterprise().getName())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .emailVerificationDate(user.getEmailVerificationDate())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    /**
     * Helper: Build auth response
     */
    private AuthResponse buildAuthResponse(User user, Enterprise enterprise, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400) // 24 hours
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().getCode())
                        .enterpriseId(enterprise.getId())
                        .enterpriseName(enterprise.getName())
                        .emailVerified(user.getEmailVerified())
                        .build())
                .build();
    }
}

    @Transactional
    public UserDTO createUser(Map<String, Object> userData) {
        String email = (String) userData.get("email");

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + email);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode((String) userData.get("password")))
                .name((String) userData.get("name"))
                .phone((String) userData.getOrDefault("phone", ""))
                .department((String) userData.getOrDefault("department", ""))
                .company((String) userData.getOrDefault("company", ""))
                .enterpriseId((String) userData.get("enterpriseId"))
                .enterpriseName((String) userData.get("enterpriseName"))
                .build();

        if (userData.get("joiningDate") != null) {
            user.setJoiningDate(LocalDate.parse((String) userData.get("joiningDate")));
        }

        @SuppressWarnings("unchecked")
        List<String> rolesList = (List<String>) userData.get("roles");
        if (rolesList != null) {
            user.setRoles(Set.copyOf(rolesList));
        }

        User saved = userRepository.save(user);
        log.info("Created user: {}", email);
        return convertToDTO(saved);
    }

    @Transactional
    public List<UserDTO> bulkCreateFromCSV(String enterpriseId, MultipartFile file) {
        List<User> users = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] row;
            boolean isHeader = true;

            while ((row = reader.readNext()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (row.length < 2) continue;

                String email = row[0].trim();
                if (userRepository.findByEmail(email).isPresent()) continue;

                User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(row[1].trim()))
                        .phone(row.length > 2 ? row[2].trim() : "")
                        .name(row.length > 3 ? row[3].trim() : "")
                        .department(row.length > 4 ? row[4].trim() : "")
                        .company(row.length > 5 ? row[5].trim() : "")
                        .enterpriseId(enterpriseId)
                        .roles(Set.of("EMPLOYEE"))
                        .build();

                if (row.length > 6 && !row[6].trim().isEmpty()) {
                    try { user.setJoiningDate(LocalDate.parse(row[6].trim())); } catch (Exception ignored) {}
                }

                users.add(user);
            }

            List<User> saved = userRepository.saveAll(users);
            log.info("Bulk created {} users from CSV", saved.size());
            return saved.stream().map(this::convertToDTO).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error processing CSV", e);
            throw new RuntimeException("Error processing CSV: " + e.getMessage());
        }
    }

    public List<UserDTO> getEnterpriseUsers(String enterpriseId) {
        return userRepository.findByEnterpriseId(enterpriseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (updates.containsKey("name")) user.setName((String) updates.get("name"));
        if (updates.containsKey("phone")) user.setPhone((String) updates.get("phone"));
        if (updates.containsKey("department")) user.setDepartment((String) updates.get("department"));
        if (updates.containsKey("enabled")) user.setEnabled((Boolean) updates.get("enabled"));

        User saved = userRepository.save(user);
        log.info("Updated user: {}", id);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user: {}", id);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .company(user.getCompany())
                .joiningDate(user.getJoiningDate())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles())
                .enterpriseId(user.getEnterpriseId())
                .enterpriseName(user.getEnterpriseName())
                .build();
    }
}
