package com.pm.userservice.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Web page/resource access levels
 * Defines which roles can access specific pages
 */
public enum PageAccessLevel {
    // System level pages - only SUPER_ADMIN
    SYSTEM_ADMIN("system_admin", "System Administration", new UserRole[]{UserRole.SUPER_ADMIN}),
    ENTERPRISE_MANAGEMENT("enterprise_management", "Enterprise Management", new UserRole[]{UserRole.SUPER_ADMIN}),

    // Enterprise level pages - CEO and above
    ENTERPRISE_DASHBOARD("enterprise_dashboard", "Enterprise Dashboard", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO}),
    ENTERPRISE_SETTINGS("enterprise_settings", "Enterprise Settings", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO}),
    BILLING_MANAGEMENT("billing_management", "Billing & Subscription", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO}),

    // HR level pages - CEO, ADMIN_HR, HR
    HR_DASHBOARD("hr_dashboard", "HR Dashboard", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR}),
    EMPLOYEE_MANAGEMENT("employee_management", "Employee Management", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR}),
    EMPLOYEE_RECORDS("employee_records", "Employee Records", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR}),
    SALARY_MANAGEMENT("salary_management", "Salary Management", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR}),
    ATTENDANCE("attendance", "Attendance Management", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR}),
    LEAVE_MANAGEMENT("leave_management", "Leave Management", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR}),
    REPORTS("reports", "HR Reports", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR}),

    // Manager level pages
    MANAGER_DASHBOARD("manager_dashboard", "Manager Dashboard", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER}),
    TEAM_MANAGEMENT("team_management", "Team Management", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER}),

    // Employee level pages
    EMPLOYEE_DASHBOARD("employee_dashboard", "Employee Dashboard", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER, UserRole.EMPLOYEE}),
    PROFILE("profile", "My Profile", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER, UserRole.EMPLOYEE}),
    MY_LEAVE("my_leave", "My Leave", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER, UserRole.EMPLOYEE}),
    MY_ATTENDANCE("my_attendance", "My Attendance", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER, UserRole.EMPLOYEE}),
    MY_PAYSLIP("my_payslip", "My Payslip", new UserRole[]{UserRole.SUPER_ADMIN, UserRole.CEO, UserRole.ADMIN_HR, UserRole.HR, UserRole.MANAGER, UserRole.EMPLOYEE});

    private final String pageId;
    private final String displayName;
    private final Set<UserRole> allowedRoles;

    PageAccessLevel(String pageId, String displayName, UserRole[] roles) {
        this.pageId = pageId;
        this.displayName = displayName;
        this.allowedRoles = new HashSet<>(Arrays.asList(roles));
    }

    public String getPageId() {
        return pageId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<UserRole> getAllowedRoles() {
        return allowedRoles;
    }

    /**
     * Check if a role has access to this page
     */
    public boolean hasAccess(UserRole role) {
        return allowedRoles.contains(role);
    }

    /**
     * Get all accessible pages for a role
     */
    public static Set<PageAccessLevel> getAccessiblePages(UserRole role) {
        Set<PageAccessLevel> pages = new HashSet<>();
        for (PageAccessLevel page : values()) {
            if (page.hasAccess(role)) {
                pages.add(page);
            }
        }
        return pages;
    }

    /**
     * Get page access level by ID
     */
    public static PageAccessLevel fromPageId(String pageId) {
        for (PageAccessLevel page : values()) {
            if (page.pageId.equalsIgnoreCase(pageId)) {
                return page;
            }
        }
        throw new IllegalArgumentException("Unknown page: " + pageId);
    }
}

