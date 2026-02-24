package com.pm.userservice.service;

import com.pm.userservice.dto.UserPageAccessDto;
import com.pm.userservice.entity.User;
import com.pm.userservice.entity.UserPageAccess;
import com.pm.userservice.enums.PageAccessLevel;
import com.pm.userservice.enums.UserRole;
import com.pm.userservice.repository.UserPageAccessRepository;
import com.pm.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user page access
 * Admin/CEO can grant or revoke page access to HR and Employees
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageAccessManagementService {

    private final UserPageAccessRepository userPageAccessRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    /**
     * Grant page access to a user
     * Only CEO, ADMIN_HR, and SUPER_ADMIN can grant access
     */
    public UserPageAccessDto grantPageAccess(String userId, PageAccessLevel page, User admin, String reason) {
        log.info("Admin {} granting page {} to user {}", admin.getEmail(), page.getPageId(), userId);

        // Validate admin has permission
        if (!canManagePageAccess(admin)) {
            throw new IllegalArgumentException("You do not have permission to manage page access");
        }

        // Validate target user exists
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Can only manage users in same enterprise
        if (!admin.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
            throw new IllegalArgumentException("Cannot manage user from different enterprise");
        }

        // Can only manage HR and Employee roles (not CEO or other admins)
        if (targetUser.getRole() == UserRole.CEO || targetUser.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot manage access for admin users");
        }

        // Check if record already exists
        var existingAccess = userPageAccessRepository.findByUserIdAndPage(userId, page);

        UserPageAccess access;
        if (existingAccess.isPresent()) {
            access = existingAccess.get();
            access.setGranted(true);
            access.setReason(reason);
            access.setGrantedBy(admin);
        } else {
            access = UserPageAccess.builder()
                    .user(targetUser)
                    .page(page)
                    .granted(true)
                    .reason(reason)
                    .grantedBy(admin)
                    .build();
        }

        UserPageAccess saved = userPageAccessRepository.save(access);
        log.info("Page access granted: {} to {} by {}", page.getPageId(), targetUser.getEmail(), admin.getEmail());

        return mapToDto(saved);
    }

    /**
     * Revoke page access from a user
     */
    public UserPageAccessDto revokePageAccess(String userId, PageAccessLevel page, User admin, String reason) {
        log.info("Admin {} revoking page {} from user {}", admin.getEmail(), page.getPageId(), userId);

        // Validate admin has permission
        if (!canManagePageAccess(admin)) {
            throw new IllegalArgumentException("You do not have permission to manage page access");
        }

        // Validate target user exists
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Can only manage users in same enterprise
        if (!admin.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
            throw new IllegalArgumentException("Cannot manage user from different enterprise");
        }

        // Can only manage HR and Employee roles
        if (targetUser.getRole() == UserRole.CEO || targetUser.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot manage access for admin users");
        }

        // Check if record exists
        var existingAccess = userPageAccessRepository.findByUserIdAndPage(userId, page);

        UserPageAccess access;
        if (existingAccess.isPresent()) {
            access = existingAccess.get();
            access.setGranted(false);
            access.setReason(reason);
            access.setGrantedBy(admin);
        } else {
            access = UserPageAccess.builder()
                    .user(targetUser)
                    .page(page)
                    .granted(false)
                    .reason(reason)
                    .grantedBy(admin)
                    .build();
        }

        UserPageAccess saved = userPageAccessRepository.save(access);
        log.info("Page access revoked: {} from {} by {}", page.getPageId(), targetUser.getEmail(), admin.getEmail());

        return mapToDto(saved);
    }

    /**
     * Check if user has access to a page (considering custom grants/revokes)
     */
    public boolean hasPageAccess(User user, PageAccessLevel page) {
        // Check if there's a custom access record
        var customAccess = userPageAccessRepository.findByUserIdAndPage(user.getId(), page);

        if (customAccess.isPresent()) {
            // If custom record exists, use it
            return customAccess.get().getGranted();
        }

        // Otherwise, use default role-based access
        return page.hasAccess(user.getRole());
    }

    /**
     * Get all custom access records for a user
     */
    public List<UserPageAccessDto> getUserCustomAccess(String userId) {
        List<UserPageAccess> records = userPageAccessRepository.findByUserId(userId);
        return records.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all pages accessible to a user (including custom grants)
     */
    public List<PageAccessLevel> getAccessiblePages(User user) {
        List<PageAccessLevel> accessiblePages = PageAccessLevel.getAccessiblePages(user.getRole())
                .stream()
                .collect(Collectors.toList());

        // Add custom granted pages
        List<UserPageAccess> customGrants = userPageAccessRepository.findCustomGrantsForUser(user.getId());
        for (UserPageAccess grant : customGrants) {
            if (!accessiblePages.contains(grant.getPage())) {
                accessiblePages.add(grant.getPage());
            }
        }

        // Remove custom revoked pages
        List<UserPageAccess> customRevokes = userPageAccessRepository.findByUserIdAndGrantedFalse(user.getId());
        for (UserPageAccess revoke : customRevokes) {
            accessiblePages.remove(revoke.getPage());
        }

        return accessiblePages;
    }

    /**
     * Get all page access records for a user in an enterprise (Admin view)
     */
    public List<UserPageAccessDto> getEnterpriseUserPageAccess(String enterpriseId, String userId, User admin) {
        // Validate admin can view this
        if (!canManagePageAccess(admin) || !admin.getEnterprise().getId().equals(enterpriseId)) {
            throw new IllegalArgumentException("You do not have permission to view this");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!targetUser.getEnterprise().getId().equals(enterpriseId)) {
            throw new IllegalArgumentException("User not in this enterprise");
        }

        List<UserPageAccess> records = userPageAccessRepository.findByUserId(userId);
        return records.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if admin can manage page access
     */
    private boolean canManagePageAccess(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN ||
               user.getRole() == UserRole.CEO ||
               user.getRole() == UserRole.ADMIN_HR;
    }

    /**
     * Map entity to DTO
     */
    private UserPageAccessDto mapToDto(UserPageAccess access) {
        return UserPageAccessDto.builder()
                .id(access.getId())
                .userId(access.getUser().getId())
                .userName(access.getUser().getFullName())
                .pageId(access.getPage().getPageId())
                .pageName(access.getPage().getDisplayName())
                .granted(access.getGranted())
                .grantedBy(access.getGrantedBy().getEmail())
                .reason(access.getReason())
                .createdAt(access.getCreatedAt())
                .modifiedAt(access.getModifiedAt())
                .build();
    }
}

