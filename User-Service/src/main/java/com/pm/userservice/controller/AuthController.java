package com.pm.userservice.controller;

import com.pm.userservice.dto.*;
import com.pm.userservice.entity.User;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * Handles user login, registration, and authentication endpoints
 * With role-based user creation (SUPER_ADMIN > CEO > HR > EMPLOYEE)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * User Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request for user: {}", loginRequest.getEmail());
        try {
            AuthResponse response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .accessToken(null)
                            .build());
        }
    }

    /**
     * Create new user with role-based permission
     * SUPER_ADMIN can create CEO users
     * CEO can create HR users only (not EMPLOYEES)
     * HR can create EMPLOYEE users only
     *
     * POST /api/auth/users
     */
    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR', 'HR')")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserCreateRequest request,
            Authentication authentication) {

        log.info("Creating new user: {} with role: {}", request.getEmail(), request.getRole().getCode());

        try {
            // Get creator user from authentication
            String creatorEmail = authentication.getName();
            User creator = userRepository.findByEmail(creatorEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

            // Create user with permission check
            UserDto user = userService.createUser(request, creator);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("User created successfully", user));

        } catch (IllegalArgumentException e) {
            log.warn("User creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "PERMISSION_DENIED"));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "USER_CREATION_FAILED"));
        }
    }

    /**
     * Get current user profile
     *
     * GET /api/auth/me
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok("User profile endpoint");
    }

    /**
     * Verify email
     *
     * POST /api/auth/verify-email/{userId}
     */
    @PostMapping("/verify-email/{userId}")
    public ResponseEntity<Void> verifyEmail(@PathVariable String userId) {
        log.info("Verifying email for user: {}", userId);
        userService.verifyUserEmail(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Change password
     *
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for user: {}", request.getUserId());
        userService.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is UP");
    }
}

