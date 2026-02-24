package com.pm.userservice.enums;

/**
 * User roles with enterprise-level hierarchy
 *
 * SUPER_ADMIN (System Level)
 *   └─ Manages enterprises and CEOs
 *
 * CEO (Enterprise Level)
 *   └─ Can create HR users only
 *
 * HR (Department Level)
 *   └─ Can create employees only
 *
 * MANAGER (Optional hierarchy)
 *   └─ Manages team reports
 *
 * EMPLOYEE (Base Level)
 *   └─ Regular user
 */
public enum UserRole {
    SUPER_ADMIN(0, "super_admin", "System Administrator", "Full system access", true, true, true, true),
    CEO(1, "ceo", "Chief Executive Officer", "Enterprise head, can create HR", true, true, false, false),
    ADMIN_HR(2, "admin_hr", "HR Administrator", "HR department head", true, false, false, false),
    HR(3, "hr", "Human Resources", "Can create employees", false, false, false, false),
    MANAGER(4, "manager", "Manager", "Team lead, manage reports", false, false, false, false),
    EMPLOYEE(5, "employee", "Employee", "Regular staff member", false, false, false, false);

    private final int level;
    private final String code;
    private final String description;
    private final String permissions;
    private final boolean canManageEnterprises;
    private final boolean canManageHR;
    private final boolean canManageEmployees;
    private final boolean canAccessPages;

    UserRole(int level, String code, String description, String permissions,
             boolean canManageEnterprises, boolean canManageHR,
             boolean canManageEmployees, boolean canAccessPages) {
        this.level = level;
        this.code = code;
        this.description = description;
        this.permissions = permissions;
        this.canManageEnterprises = canManageEnterprises;
        this.canManageHR = canManageHR;
        this.canManageEmployees = canManageEmployees;
        this.canAccessPages = canAccessPages;
    }

    public int getLevel() {
        return level;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getPermissions() {
        return permissions;
    }

    public boolean canManageEnterprises() {
        return canManageEnterprises;
    }

    public boolean canManageHR() {
        return canManageHR;
    }

    public boolean canManageEmployees() {
        return canManageEmployees;
    }

    public boolean canAccessPages() {
        return canAccessPages;
    }

    /**
     * Check if this role has higher privilege than another
     */
    public boolean hasHigherPrivilegeThan(UserRole other) {
        return this.level < other.level;
    }

    /**
     * Check if this role can create users of given role
     */
    public boolean canCreateRole(UserRole targetRole) {
        if (this == SUPER_ADMIN) {
            return targetRole == CEO || targetRole == ADMIN_HR;
        }
        if (this == CEO) {
            return targetRole == ADMIN_HR || targetRole == HR;
        }
        if (this == ADMIN_HR || this == HR) {
            return targetRole == EMPLOYEE || targetRole == MANAGER;
        }
        return false;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + code);
    }
}


