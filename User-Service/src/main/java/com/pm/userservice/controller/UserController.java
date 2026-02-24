package com.pm.userservice.controller;

import com.pm.userservice.dto.ApiResponse;
import com.pm.userservice.dto.UserDto;
import com.pm.userservice.enums.UserRole;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management Controller
 * Provides endpoints for user management with role-based access control
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get user by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        log.info("Fetching user: {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by email in enterprise
     * GET /api/users/email/{email}?enterpriseId={enterpriseId}
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserByEmail(
            @PathVariable String email,
            @RequestParam String enterpriseId) {
        log.info("Fetching user by email: {} in enterprise: {}", email, enterpriseId);
        UserDto user = userService.getUserByEmailInEnterprise(email, enterpriseId);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users in enterprise
     * GET /api/users/enterprise/{enterpriseId}
     */
    @GetMapping("/enterprise/{enterpriseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_HR', 'HR')")
    public ResponseEntity<List<UserDto>> getAllUsersInEnterprise(@PathVariable String enterpriseId) {
        log.info("Fetching all users in enterprise: {}", enterpriseId);
        List<UserDto> users = userService.getAllUsersInEnterprise(enterpriseId);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role in enterprise
     * GET /api/users/enterprise/{enterpriseId}/role/{role}
     */
    @GetMapping("/enterprise/{enterpriseId}/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_HR', 'HR')")
    public ResponseEntity<List<UserDto>> getUsersByRole(
            @PathVariable String enterpriseId,
            @PathVariable String role) {
        log.info("Fetching users with role: {} in enterprise: {}", role, enterpriseId);
        UserRole userRole = UserRole.fromCode(role);
        List<UserDto> users = userService.getUsersByRoleInEnterprise(enterpriseId, userRole);
        return ResponseEntity.ok(users);
    }

    /**
     * Get all HR users in enterprise
     * GET /api/users/enterprise/{enterpriseId}/hr
     */
    @GetMapping("/enterprise/{enterpriseId}/hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_HR')")
    public ResponseEntity<List<UserDto>> getHRUsers(@PathVariable String enterpriseId) {
        log.info("Fetching HR users in enterprise: {}", enterpriseId);
        List<UserDto> users = userService.getHRUsersInEnterprise(enterpriseId);
        return ResponseEntity.ok(users);
    }

    /**
     * Get reports of a manager
     * GET /api/users/{managerId}/reports?enterpriseId={enterpriseId}
     */
    @GetMapping("/{managerId}/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_HR', 'MANAGER')")
    public ResponseEntity<List<UserDto>> getManagerReports(
            @PathVariable String managerId,
            @RequestParam String enterpriseId) {
        log.info("Fetching reports for manager: {} in enterprise: {}", managerId, enterpriseId);
        List<UserDto> reports = userService.getManagerReports(managerId, enterpriseId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Update user
     * PUT /api/users/{userId}
     */
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String userId,
            @RequestBody UserDto updateRequest) {
        log.info("Updating user: {}", userId);
        UserDto updatedUser = userService.updateUser(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deactivate user (soft delete) with permission check
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> deactivateUser(
            @PathVariable String userId,
            Authentication authentication) {

        log.info("Deactivating user: {}", userId);

        try {
            // Get actor from authentication
            String actorEmail = authentication.getName();
            var actorOptional = userRepository.findByEmail(actorEmail);

            if (actorOptional.isEmpty()) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("User not found", "USER_NOT_FOUND"));
            }

            var actor = actorOptional.get();
            userService.deactivateUser(userId, actor);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Deactivation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "PERMISSION_DENIED"));
        }
    }

    /**
     * Health check endpoint
     * GET /api/users/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is UP");
    }
}

        return ResponseEntity.ok(ApiResponse.success("Users created successfully", users));
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<List<UserDTO>> getEnterpriseUsers(@PathVariable String enterpriseId) {
        return ResponseEntity.ok(userService.getEnterpriseUsers(enterpriseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        UserDTO user = userService.updateUser(id, updates);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}