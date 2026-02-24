package com.pm.userservice.controller;

import com.pm.userservice.dto.PageAccessDto;
import com.pm.userservice.dto.ApiResponse;
import com.pm.userservice.enums.PageAccessLevel;
import com.pm.userservice.enums.UserRole;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Page Access Controller
 * Provides endpoints for checking page access and retrieving available pages
 */
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
@Slf4j
public class PageAccessController {

    private final PermissionService permissionService;
    private final UserRepository userRepository;

    /**
     * Get all pages accessible by the current user
     * GET /api/pages/accessible
     */
    @GetMapping("/accessible")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PageAccessDto>> getAccessiblePages(Authentication authentication) {
        String email = authentication.getName();

        var userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = userOptional.get();
        Set<PageAccessLevel> accessiblePages = permissionService.getAccessiblePages(user);

        List<PageAccessDto> pages = accessiblePages.stream()
                .map(page -> PageAccessDto.builder()
                        .pageId(page.getPageId())
                        .displayName(page.getDisplayName())
                        .allowedRoles(page.getAllowedRoles().stream()
                                .map(UserRole::getCode)
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toList());

        log.info("User {} retrieved {} accessible pages", email, pages.size());
        return ResponseEntity.ok(pages);
    }

    /**
     * Check if user can access a specific page
     * GET /api/pages/check/{pageId}
     */
    @GetMapping("/check/{pageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> checkPageAccess(
            @PathVariable String pageId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            var userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("User not found", "USER_NOT_FOUND"));
            }

            var user = userOptional.get();
            PageAccessLevel page = PageAccessLevel.fromPageId(pageId);
            boolean hasAccess = permissionService.canAccessPage(user, page);

            log.info("User {} checked access to page {} - {}",
                    email, pageId, hasAccess ? "granted" : "denied");

            return ResponseEntity.ok(ApiResponse.success(
                    hasAccess ? "Access granted" : "Access denied",
                    hasAccess));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid page ID: {}", pageId);
            return ResponseEntity.ok(ApiResponse.error("Invalid page", "INVALID_PAGE"));
        }
    }

    /**
     * Get all pages with their access levels
     * GET /api/pages/all (Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PageAccessDto>> getAllPages() {
        List<PageAccessDto> pages = new java.util.ArrayList<>();

        for (PageAccessLevel page : PageAccessLevel.values()) {
            pages.add(PageAccessDto.builder()
                    .pageId(page.getPageId())
                    .displayName(page.getDisplayName())
                    .allowedRoles(page.getAllowedRoles().stream()
                            .map(UserRole::getCode)
                            .collect(Collectors.toSet()))
                    .build());
        }

        return ResponseEntity.ok(pages);
    }

    /**
     * Get all pages accessible by a specific role
     * GET /api/pages/role/{roleCode}
     */
    @GetMapping("/role/{roleCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO')")
    public ResponseEntity<ApiResponse<List<PageAccessDto>>> getPagesByRole(@PathVariable String roleCode) {
        try {
            UserRole role = UserRole.fromCode(roleCode);
            Set<PageAccessLevel> accessiblePages = PageAccessLevel.getAccessiblePages(role);

            List<PageAccessDto> pages = accessiblePages.stream()
                    .map(page -> PageAccessDto.builder()
                            .pageId(page.getPageId())
                            .displayName(page.getDisplayName())
                            .allowedRoles(page.getAllowedRoles().stream()
                                    .map(UserRole::getCode)
                                    .collect(Collectors.toSet()))
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    role.getDescription() + " has " + pages.size() + " accessible pages",
                    pages));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid role code: {}", roleCode);
            return ResponseEntity.ok(ApiResponse.error("Invalid role", "INVALID_ROLE"));
        }
    }

    /**
     * Get role information with permissions
     * GET /api/pages/role-info/{roleCode}
     */
    @GetMapping("/role-info/{roleCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RoleInfoDto>> getRoleInfo(@PathVariable String roleCode) {
        try {
            UserRole role = UserRole.fromCode(roleCode);
            Set<PageAccessLevel> pages = PageAccessLevel.getAccessiblePages(role);

            RoleInfoDto info = RoleInfoDto.builder()
                    .roleCode(role.getCode())
                    .roleName(role.getDescription())
                    .permissions(role.getPermissions())
                    .canManageEnterprises(role.canManageEnterprises())
                    .canManageHR(role.canManageHR())
                    .canManageEmployees(role.canManageEmployees())
                    .canAccessPages(role.canAccessPages())
                    .totalAccessiblePages(pages.size())
                    .accessiblePageIds(pages.stream()
                            .map(PageAccessLevel::getPageId)
                            .collect(Collectors.toSet()))
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Role information retrieved", info));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid role code: {}", roleCode);
            return ResponseEntity.ok(ApiResponse.error("Invalid role", "INVALID_ROLE"));
        }
    }

    /**
     * DTO for role information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleInfoDto {
        private String roleCode;
        private String roleName;
        private String permissions;
        private boolean canManageEnterprises;
        private boolean canManageHR;
        private boolean canManageEmployees;
        private boolean canAccessPages;
        private int totalAccessiblePages;
        private Set<String> accessiblePageIds;
    }
}

