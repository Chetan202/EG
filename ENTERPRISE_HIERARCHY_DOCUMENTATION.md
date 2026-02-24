# 🏢 Enterprise-Level Role Hierarchy - Complete Implementation

## Overview

Implemented a **production-grade enterprise hierarchy system** with:

```
SUPER_ADMIN (System Level)
    ↓
    ├─→ Can create CEOs
    ├─→ Can manage enterprises
    └─→ Access to ALL pages
    
CEO (Enterprise Level)
    ↓
    ├─→ Can create HR users ONLY (not employees)
    ├─→ Manage own enterprise
    └─→ Access to enterprise pages
    
ADMIN_HR (HR Department Head)
    ↓
    ├─→ Can create HR and Employee users
    ├─→ Manage all HR functions
    └─→ Access to HR pages
    
HR (HR Specialist)
    ↓
    ├─→ Can create Employee users ONLY
    ├─→ Manage employee records
    └─→ Access to HR pages
    
MANAGER (Team Lead)
    ↓
    ├─→ Manage own team
    ├─→ View team reports
    └─→ Access to manager pages
    
EMPLOYEE (Regular Staff)
    ↓
    └─→ Basic access (profile, leave, attendance)
```

---

## Key Features

### 1. Role-Based User Creation

**SUPER_ADMIN** can create:
- ✅ CEO users
- ✅ ADMIN_HR users
- ✅ Manage enterprises

**CEO** can create:
- ✅ ADMIN_HR users (HR department head)
- ✅ HR users (HR specialists)
- ❌ CANNOT create employees

**ADMIN_HR** can create:
- ✅ HR users
- ✅ EMPLOYEE users
- ✅ MANAGER users

**HR** can create:
- ✅ EMPLOYEE users
- ✅ MANAGER users

### 2. Enterprise Isolation

- CEO can ONLY create users in their own enterprise
- HR can ONLY manage users in their own enterprise
- SUPER_ADMIN can manage any enterprise

```java
// Example: CEO attempting to create user in different enterprise
CEO at Enterprise-A tries to create user for Enterprise-B
Result: ❌ REJECTED - Different enterprise!
```

### 3. Dynamic Page Access

Pages are categorized by access level:

```
SYSTEM LEVEL (SUPER_ADMIN only)
├── System Administration
├── Enterprise Management

ENTERPRISE LEVEL (CEO and above)
├── Enterprise Dashboard
├── Enterprise Settings
├── Billing Management

HR LEVEL (CEO, ADMIN_HR, HR)
├── HR Dashboard
├── Employee Management
├── Employee Records
├── Salary Management
├── Attendance
├── Leave Management
├── Reports

MANAGER LEVEL (Managers and above)
├── Manager Dashboard
├── Team Management

EMPLOYEE LEVEL (All users)
├── Employee Dashboard
├── My Profile
├── My Leave
├── My Attendance
├── My Payslip
```

---

## New Components Created

### 1. **UserRole Enum** (Updated)
```
SUPER_ADMIN - System administrator
CEO - Chief Executive Officer (Enterprise head)
ADMIN_HR - HR Administrator (Department head)
HR - Human Resources (HR Specialist)
MANAGER - Manager (Team lead)
EMPLOYEE - Employee (Regular staff)
```

**New Methods:**
- `canCreateRole(UserRole targetRole)` - Check if can create specific role
- `canManageEnterprises()` - Check if can manage enterprises
- `canManageHR()` - Check if can manage HR
- `canManageEmployees()` - Check if can manage employees
- `canAccessPages()` - Check if can access website pages

### 2. **PageAccessLevel Enum** (New)
Defines all website pages with access control:

```java
// System pages
SYSTEM_ADMIN, ENTERPRISE_MANAGEMENT

// Enterprise pages
ENTERPRISE_DASHBOARD, ENTERPRISE_SETTINGS, BILLING_MANAGEMENT

// HR pages
HR_DASHBOARD, EMPLOYEE_MANAGEMENT, EMPLOYEE_RECORDS, 
SALARY_MANAGEMENT, ATTENDANCE, LEAVE_MANAGEMENT, REPORTS

// Manager pages
MANAGER_DASHBOARD, TEAM_MANAGEMENT

// Employee pages
EMPLOYEE_DASHBOARD, PROFILE, MY_LEAVE, MY_ATTENDANCE, MY_PAYSLIP
```

### 3. **PermissionService** (New)
Handles all permission logic:

```java
// Check if user can create another user with specific role
canCreateUser(User creator, UserRole targetRole, String enterpriseId)

// Check if user can access a page
canAccessPage(User user, PageAccessLevel page)

// Check if user can manage another user
canManageUser(User actor, User targetUser)

// Check if user can deactivate another user
canDeactivateUser(User actor, User targetUser)

// Check if user can assign manager
canAssignManager(User actor, User targetUser, User newManager)

// Get all accessible pages for a user
getAccessiblePages(User user)
```

### 4. **PageAccessController** (New)
API endpoints for page access management:

```
GET  /api/pages/accessible           - Get user's accessible pages
GET  /api/pages/check/{pageId}       - Check access to specific page
GET  /api/pages/all                  - Get all pages (Admin only)
GET  /api/pages/role/{roleCode}      - Get pages for a role
GET  /api/pages/role-info/{roleCode} - Get role information
```

### 5. **PageAccessDto** (New)
DTO for page information

---

## API Examples

### Example 1: SUPER_ADMIN Creates CEO

```bash
POST /api/auth/users
Authorization: Bearer <super-admin-token>

{
  "email": "ceo@company.com",
  "firstName": "John",
  "lastName": "CEO",
  "employeeId": "CEO-001",
  "password": "SecurePass123",
  "role": "CEO",
  "enterpriseId": "ent-001"
}

Response: 201 Created ✓
```

### Example 2: CEO Tries to Create EMPLOYEE

```bash
POST /api/auth/users
Authorization: Bearer <ceo-token>

{
  "email": "emp@company.com",
  "firstName": "Jane",
  "lastName": "Employee",
  "employeeId": "EMP-001",
  "password": "Pass123",
  "role": "EMPLOYEE",  ← CEO cannot create this!
  "enterpriseId": "ent-001"
}

Response: 403 Forbidden
Message: "You do not have permission to create user with role: employee"
```

### Example 3: CEO Creates HR User

```bash
POST /api/auth/users
Authorization: Bearer <ceo-token>

{
  "email": "hr@company.com",
  "firstName": "Sarah",
  "lastName": "HR-Manager",
  "employeeId": "HR-001",
  "password": "Pass123",
  "role": "HR",  ← CEO CAN create HR!
  "enterpriseId": "ent-001"
}

Response: 201 Created ✓
```

### Example 4: HR Creates Employee

```bash
POST /api/auth/users
Authorization: Bearer <hr-token>

{
  "email": "emp@company.com",
  "firstName": "Bob",
  "lastName": "Developer",
  "employeeId": "EMP-001",
  "password": "Pass123",
  "role": "EMPLOYEE",  ← HR CAN create employees!
  "enterpriseId": "ent-001"
}

Response: 201 Created ✓
```

### Example 5: Get Accessible Pages for Employee

```bash
GET /api/pages/accessible
Authorization: Bearer <employee-token>

Response: 200 OK
{
  "success": true,
  "message": "...",
  "data": [
    { "pageId": "employee_dashboard", "displayName": "Employee Dashboard", ... },
    { "pageId": "profile", "displayName": "My Profile", ... },
    { "pageId": "my_leave", "displayName": "My Leave", ... },
    { "pageId": "my_attendance", "displayName": "My Attendance", ... },
    { "pageId": "my_payslip", "displayName": "My Payslip", ... }
  ]
}
```

### Example 6: Check Page Access

```bash
GET /api/pages/check/salary_management
Authorization: Bearer <employee-token>

Response: 200 OK
{
  "success": true,
  "message": "Access denied",
  "data": false  ← Employee cannot access salary management!
}
```

### Example 7: Employee Tries to Deactivate Another User

```bash
DELETE /api/users/another-user-id
Authorization: Bearer <employee-token>

Response: 403 Forbidden
Message: "You do not have permission to deactivate this user"
```

---

## Database Schema

No database changes needed - existing `users` table is used with:

```
users.role (Enum)
  - SUPER_ADMIN
  - CEO
  - ADMIN_HR
  - HR
  - MANAGER
  - EMPLOYEE

users.enterprise_id (Foreign Key)
  - Multi-tenant isolation
  - CEO can only manage own enterprise
```

---

## New Endpoints

### 1. Updated: POST `/api/auth/users` (Create User)
Now with role-based permission checks:
- SUPER_ADMIN → CEO, ADMIN_HR
- CEO → ADMIN_HR, HR
- ADMIN_HR → HR, EMPLOYEE, MANAGER
- HR → EMPLOYEE, MANAGER

### 2. Updated: DELETE `/api/users/{userId}` (Deactivate)
Now with permission checks:
- SUPER_ADMIN → Anyone
- CEO → HR and below (not other CEOs)
- ADMIN_HR → HR, EMPLOYEE, MANAGER
- HR → EMPLOYEE, MANAGER

### 3. New: GET `/api/pages/accessible`
Returns all pages user can access

### 4. New: GET `/api/pages/check/{pageId}`
Check if user can access specific page

### 5. New: GET `/api/pages/role/{roleCode}`
Get all pages for a specific role

### 6. New: GET `/api/pages/role-info/{roleCode}`
Get detailed role information and permissions

### 7. New: GET `/api/pages/all`
Get all pages in system (Admin only)

---

## Permission Matrix

| Action | SUPER_ADMIN | CEO | ADMIN_HR | HR | MANAGER | EMPLOYEE |
|--------|-------------|-----|----------|-----|---------|----------|
| Create CEO | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Create ADMIN_HR | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| Create HR | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| Create EMPLOYEE | ✓ | ✗ | ✓ | ✓ | ✗ | ✗ |
| Create MANAGER | ✓ | ✗ | ✓ | ✓ | ✗ | ✗ |
| Deactivate any | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Deactivate HR+ | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| Deactivate EMP+ | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| Manage Enterprise | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Access Pages | ALL | Enterprise+ | HR+ | HR+ | Manager+ | Employee |

---

## Page Access Matrix

| Page | SUPER_ADMIN | CEO | ADMIN_HR | HR | MANAGER | EMPLOYEE |
|------|-------------|-----|----------|-----|---------|----------|
| System Admin | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Enterprise Mgmt | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Enterprise Dashboard | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| Enterprise Settings | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| Billing | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| HR Dashboard | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| Employee Mgmt | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| Salary Mgmt | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| Attendance | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| Reports | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| Manager Dashboard | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| Team Management | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| Employee Dashboard | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Profile | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| My Leave | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| My Payslip | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

---

## Integration with Web Frontend

The website team can:

1. **Get user's accessible pages:**
```javascript
// After login, get accessible pages
fetch('/api/pages/accessible', {
  headers: { 'Authorization': 'Bearer ' + token }
})
.then(r => r.json())
.then(data => {
  // Show only these pages in navigation
  renderNavigation(data.data);
});
```

2. **Check page access before navigation:**
```javascript
fetch(`/api/pages/check/${pageId}`, {
  headers: { 'Authorization': 'Bearer ' + token }
})
.then(r => r.json())
.then(data => {
  if (data.data) {
    // Load page
  } else {
    // Show "Access Denied"
  }
});
```

3. **Display role-specific dashboards:**
```javascript
const userRole = jwtDecode(token).role;
if (userRole === 'SUPER_ADMIN') {
  showSystemAdminDashboard();
} else if (userRole === 'CEO') {
  showEnterpriseDashboard();
} else if (userRole === 'HR' || userRole === 'ADMIN_HR') {
  showHRDashboard();
} // ... etc
```

---

## Deployment Ready

✅ **Production Features:**
- Enterprise-level role hierarchy
- Dynamic page access control
- Permission-based user creation
- Multi-tenant data isolation
- Comprehensive audit logging
- Secure error handling
- RESTful API design

✅ **Microservice Ready:**
- Works independently
- Other services can query pages via APIs
- No tight coupling
- Scalable architecture

---

## Files Modified/Created

**Modified:**
- `UserRole.java` - Added new roles and permission methods
- `UserService.java` - Added PermissionService checks
- `AuthController.java` - Added creator context
- `UserController.java` - Added actor context for deactivation

**Created:**
- `PageAccessLevel.java` - Page access definitions
- `PermissionService.java` - Permission logic
- `PageAccessController.java` - Page access API endpoints
- `PageAccessDto.java` - DTO for page information

---

## Security & Compliance

✅ **Security:**
- Role-based access control (RBAC)
- Multi-tenant isolation
- Permission checks on every operation
- No bypass mechanisms
- Audit logging for all actions

✅ **Enterprise-Level:**
- CEO cannot create employees
- HR cannot access system admin pages
- Employee cannot deactivate anyone
- Manager limited to own team
- Complete role hierarchy enforcement

**Ready for production deployment!** 🚀

