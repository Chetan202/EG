package com.pm.userservice.service;

import com.pm.userservice.dto.UserDto;
import com.pm.userservice.entity.User;
import com.pm.userservice.enums.PageAccessLevel;
import com.pm.userservice.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Permission & Access Control Service
 * Handles role-based permissions and page access
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    /**
     * Check if a user can create another user with a specific role
     */
    public boolean canCreateUser(User creator, UserRole targetRole, String targetEnterpriseId) {
        // Validate creator has permission
        if (!creator.getRole().canCreateRole(targetRole)) {
            log.warn("User {} attempted to create user with role {}",
                    creator.getEmail(), targetRole.getCode());
            return false;
        }

        // CEO can only create users in their own enterprise
        if (creator.getRole() == UserRole.CEO &&
                !creator.getEnterprise().getId().equals(targetEnterpriseId)) {
            log.warn("CEO {} attempted to create user in different enterprise",
                    creator.getEmail());
            return false;
        }

        // HR can only create users in their own enterprise
        if ((creator.getRole() == UserRole.HR || creator.getRole() == UserRole.ADMIN_HR) &&
                !creator.getEnterprise().getId().equals(targetEnterpriseId)) {
            log.warn("HR {} attempted to create user in different enterprise",
                    creator.getEmail());
            return false;
        }

        return true;
    }

    /**
     * Check if user can access a specific page
     */
    public boolean canAccessPage(User user, PageAccessLevel page) {
        boolean hasAccess = page.hasAccess(user.getRole());
        if (!hasAccess) {
            log.warn("User {} with role {} attempted to access page {}",
                    user.getEmail(), user.getRole().getCode(), page.getPageId());
        }
        return hasAccess;
    }

    /**
     * Check if user can manage another user
     */
    public boolean canManageUser(User actor, User targetUser) {
        // SUPER_ADMIN can manage anyone
        if (actor.getRole() == UserRole.SUPER_ADMIN) {
            return true;
        }

        // Must be in same enterprise
        if (!actor.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
            log.warn("User {} attempted to manage user from different enterprise",
                    actor.getEmail());
            return false;
        }

        // CEO can manage HR and below
        if (actor.getRole() == UserRole.CEO) {
            return targetUser.getRole() != UserRole.CEO &&
                    targetUser.getRole() != UserRole.SUPER_ADMIN;
        }

        // ADMIN_HR/HR can manage EMPLOYEE and MANAGER only
        if (actor.getRole() == UserRole.ADMIN_HR || actor.getRole() == UserRole.HR) {
            return targetUser.getRole() == UserRole.EMPLOYEE ||
                    targetUser.getRole() == UserRole.MANAGER;
        }

        return false;
    }

    /**
     * Check if user can delete/deactivate another user
     */
    public boolean canDeactivateUser(User actor, User targetUser) {
        // SUPER_ADMIN can deactivate anyone
        if (actor.getRole() == UserRole.SUPER_ADMIN) {
            return true;
        }

        // Cannot deactivate self
        if (actor.getId().equals(targetUser.getId())) {
            log.warn("User {} attempted to deactivate self", actor.getEmail());
            return false;
        }

        // Must be in same enterprise
        if (!actor.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
            return false;
        }

        // CEO can deactivate HR and below (not other CEOs)
        if (actor.getRole() == UserRole.CEO) {
            return targetUser.getRole() != UserRole.CEO &&
                    targetUser.getRole() != UserRole.ADMIN_HR;
        }

        // ADMIN_HR can deactivate HR, EMPLOYEE, MANAGER
        if (actor.getRole() == UserRole.ADMIN_HR) {
            return targetUser.getRole() == UserRole.HR ||
                    targetUser.getRole() == UserRole.EMPLOYEE ||
                    targetUser.getRole() == UserRole.MANAGER;
        }

        // HR can only deactivate EMPLOYEE and MANAGER
        if (actor.getRole() == UserRole.HR) {
            return targetUser.getRole() == UserRole.EMPLOYEE ||
                    targetUser.getRole() == UserRole.MANAGER;
        }

        return false;
    }

    /**
     * Check if user can manage enterprise
     */
    public boolean canManageEnterprise(User user) {
        return user.getRole().canManageEnterprises();
    }

    /**
     * Get all accessible pages for user
     */
    public Set<PageAccessLevel> getAccessiblePages(User user) {
        return PageAccessLevel.getAccessiblePages(user.getRole());
    }

    /**
     * Check if user can view another user's details
     */
    public boolean canViewUserDetails(User viewer, User targetUser) {
        // SUPER_ADMIN can view anyone
        if (viewer.getRole() == UserRole.SUPER_ADMIN) {
            return true;
        }

        // Same user can view own details
        if (viewer.getId().equals(targetUser.getId())) {
            return true;
        }

        // Must be in same enterprise
        if (!viewer.getEnterprise().getId().equals(targetUser.getEnterprise().getId())) {
            return false;
        }

        // CEO can view all in enterprise
        if (viewer.getRole() == UserRole.CEO) {
            return true;
        }

        // HR/ADMIN_HR can view all in enterprise
        if (viewer.getRole() == UserRole.ADMIN_HR || viewer.getRole() == UserRole.HR) {
            return true;
        }

        // Manager can view own team
        if (viewer.getRole() == UserRole.MANAGER) {
            return targetUser.getManager() != null &&
                    targetUser.getManager().getId().equals(viewer.getId());
        }

        return false;
    }

    /**
     * Check if user can assign manager to another user
     */
    public boolean canAssignManager(User actor, User targetUser, User newManager) {
        // Only SUPER_ADMIN, CEO, ADMIN_HR can assign managers
        if (!canManageUser(actor, targetUser)) {
            return false;
        }

        // Manager must be in same enterprise
        if (!actor.getEnterprise().getId().equals(newManager.getEnterprise().getId())) {
            return false;
        }

        // Manager must have MANAGER role (or ADMIN_HR, CEO for override)
        return newManager.getRole() == UserRole.MANAGER ||
                newManager.getRole() == UserRole.ADMIN_HR ||
                newManager.getRole() == UserRole.CEO;
    }
}

