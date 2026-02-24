package com.pm.userservice.controller;

import com.pm.userservice.dto.ApiResponse;
import com.pm.userservice.dto.PageAccessGrantRequest;
import com.pm.userservice.dto.UserPageAccessDto;
import com.pm.userservice.entity.User;
import com.pm.userservice.enums.PageAccessLevel;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.service.PageAccessManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Page Access Management Controller
 * Admin/CEO can grant or revoke page access to HR and Employees
 */
@RestController
@RequestMapping("/api/page-access")
@RequiredArgsConstructor
@Slf4j
public class PageAccessManagementController {

    private final PageAccessManagementService pageAccessManagementService;
    private final UserRepository userRepository;

    /**
     * Grant page access to a user
     * POST /api/page-access/grant
     *
     * Only SUPER_ADMIN, CEO, ADMIN_HR can grant access
     */
    @PostMapping("/grant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> grantPageAccess(
            @Valid @RequestBody PageAccessGrantRequest request,
            Authentication authentication) {

        try {
            log.info("Grant page access request: user={}, page={}", request.getUserId(), request.getPageId());

            // Get admin from token
            String adminEmail = authentication.getName();
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            // Get page access level
            PageAccessLevel page = PageAccessLevel.fromPageId(request.getPageId());

            // Grant access
            UserPageAccessDto result = pageAccessManagementService.grantPageAccess(
                    request.getUserId(), page, admin, request.getReason());

            return ResponseEntity.ok(ApiResponse.success(
                    "Page access granted successfully",
                    result));

        } catch (IllegalArgumentException e) {
            log.warn("Grant access failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("Error granting page access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error granting access", "INTERNAL_ERROR"));
        }
    }

    /**
     * Revoke page access from a user
     * POST /api/page-access/revoke
     */
    @PostMapping("/revoke")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> revokePageAccess(
            @Valid @RequestBody PageAccessGrantRequest request,
            Authentication authentication) {

        try {
            log.info("Revoke page access request: user={}, page={}", request.getUserId(), request.getPageId());

            // Get admin from token
            String adminEmail = authentication.getName();
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            // Get page access level
            PageAccessLevel page = PageAccessLevel.fromPageId(request.getPageId());

            // Revoke access
            UserPageAccessDto result = pageAccessManagementService.revokePageAccess(
                    request.getUserId(), page, admin, request.getReason());

            return ResponseEntity.ok(ApiResponse.success(
                    "Page access revoked successfully",
                    result));

        } catch (IllegalArgumentException e) {
            log.warn("Revoke access failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("Error revoking page access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error revoking access", "INTERNAL_ERROR"));
        }
    }

    /**
     * Get all accessible pages for a user (including custom grants)
     * GET /api/page-access/user/{userId}/pages
     */
    @GetMapping("/user/{userId}/pages")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> getUserAccessiblePages(
            @PathVariable String userId,
            Authentication authentication) {

        try {
            // Get admin from token
            String adminEmail = authentication.getName();
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            // Get target user
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Verify same enterprise
            if (!admin.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Cannot access user from different enterprise", "FORBIDDEN"));
            }

            // Get accessible pages
            List<String> accessiblePages = pageAccessManagementService.getAccessiblePages(targetUser)
                    .stream()
                    .map(PageAccessLevel::getPageId)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    "User has " + accessiblePages.size() + " accessible pages",
                    accessiblePages));

        } catch (IllegalArgumentException e) {
            log.warn("Error getting accessible pages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        }
    }

    /**
     * Get custom access records for a user (only admin/CEO grants and revokes)
     * GET /api/page-access/user/{userId}/custom
     */
    @GetMapping("/user/{userId}/custom")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> getUserCustomAccess(
            @PathVariable String userId,
            Authentication authentication) {

        try {
            // Get admin from token
            String adminEmail = authentication.getName();
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            // Get target user
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Verify same enterprise
            if (!admin.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Cannot access user from different enterprise", "FORBIDDEN"));
            }

            // Get custom access records
            List<UserPageAccessDto> customAccess = pageAccessManagementService.getEnterpriseUserPageAccess(
                    admin.getEnterprise().getId(), userId, admin);

            return ResponseEntity.ok(ApiResponse.success(
                    "Retrieved " + customAccess.size() + " custom access records",
                    customAccess));

        } catch (IllegalArgumentException e) {
            log.warn("Error getting custom access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        }
    }

    /**
     * Get all pages with access status for a user
     * GET /api/page-access/user/{userId}/all-pages
     */
    @GetMapping("/user/{userId}/all-pages")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> getUserAllPagesWithStatus(
            @PathVariable String userId,
            Authentication authentication) {

        try {
            // Get admin from token
            String adminEmail = authentication.getName();
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            // Get target user
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Verify same enterprise
            if (!admin.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Cannot access user from different enterprise", "FORBIDDEN"));
            }

            // Get all pages with access status
            List<?> pagesWithStatus = PageAccessLevel.values()
                    .stream()
                    .map(page -> new Object() {
                        public final String pageId = page.getPageId();
                        public final String displayName = page.getDisplayName();
                        public final Boolean hasAccess = pageAccessManagementService.hasPageAccess(targetUser, page);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    "All pages with access status",
                    pagesWithStatus));

        } catch (IllegalArgumentException e) {
            log.warn("Error getting pages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        }
    }

    /**
     * Batch grant pages to a user
     * POST /api/page-access/grant-batch
     */
    @PostMapping("/grant-batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CEO', 'ADMIN_HR')")
    public ResponseEntity<?> grantPagesBatch(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        try {
            String userId = (String) request.get("userId");
            List<String> pageIds = (List<String>) request.get("pageIds");
            String reason = (String) request.getOrDefault("reason", null);

            log.info("Batch grant pages: user={}, pages={}", userId, pageIds.size());

            // Get admin
            String adminEmail = authentication.getName();
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            List<UserPageAccessDto> results = pageIds.stream()
                    .map(pageId -> {
                        try {
                            PageAccessLevel page = PageAccessLevel.fromPageId(pageId);
                            return pageAccessManagementService.grantPageAccess(userId, page, admin, reason);
                        } catch (Exception e) {
                            log.warn("Error granting page {}: {}", pageId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    "Granted " + results.size() + " pages",
                    results));

        } catch (Exception e) {
            log.error("Error in batch grant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error processing batch grant", "INTERNAL_ERROR"));
        }
    }
}


