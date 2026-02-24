# Admin Page Access Management - Feature Documentation

## Overview

**Admin can now dynamically grant or revoke page access** to HR and Employee users. This gives administrators complete control over who can access which pages, beyond the default role-based access.

---

## How It Works

### Default Behavior (Role-Based)
```
Before:
- EMPLOYEE → 5 pages (default)
- HR → 11 pages (default)
- CEO → 14 pages (default)
```

### With Custom Access Management
```
Admin CAN:
├─ Grant additional pages to Employee
├─ Revoke pages from HR
├─ Revoke pages from Employee
└─ Grant special access for temporary needs

Admin CANNOT:
├─ Manage other Admin/CEO access
└─ Access outside their enterprise
```

---

## Key Concepts

### 1. Custom Access Records
```
User: John (Employee)
Default Access: 5 pages
Custom Grant: employee_dashboard (already has it)
Custom Revoke: my_payslip (removed access)
Final Access: 4 pages
```

### 2. Grant vs Revoke
```
Grant: Add access beyond default role
Revoke: Remove access from default role or previously granted

Example:
- Employee role has: profile, my_leave, my_attendance, my_payslip, employee_dashboard
- Admin GRANTS: salary_management (not in employee role)
- Admin REVOKES: my_payslip (in employee role)
- Final: profile, my_leave, my_attendance, salary_management, employee_dashboard
```

### 3. Enterprise Isolation
```
CEO at Enterprise-A can ONLY:
├─ Manage HR users in Enterprise-A
├─ Manage Employee users in Enterprise-A
└─ Grant/revoke pages within Enterprise-A

CEO CANNOT:
├─ Manage users in Enterprise-B
└─ Grant/revoke pages for Enterprise-B
```

---

## New API Endpoints

### 1. Grant Page Access
```
POST /api/page-access/grant
Authorization: Bearer <ADMIN_TOKEN>

{
  "userId": "user-123",
  "pageId": "salary_management",
  "action": "grant",
  "reason": "Special access for quarterly review"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": "access-123",
    "userId": "user-123",
    "userName": "John Doe",
    "pageId": "salary_management",
    "pageName": "Salary Management",
    "granted": true,
    "grantedBy": "admin@company.com",
    "reason": "Special access for quarterly review",
    "createdAt": "2026-02-24T10:30:00",
    "modifiedAt": "2026-02-24T10:30:00"
  }
}
```

### 2. Revoke Page Access
```
POST /api/page-access/revoke
Authorization: Bearer <ADMIN_TOKEN>

{
  "userId": "user-123",
  "pageId": "my_payslip",
  "action": "revoke",
  "reason": "Temporary restriction during investigation"
}

Response: 200 OK
```

### 3. Get User's Accessible Pages
```
GET /api/page-access/user/{userId}/pages
Authorization: Bearer <ADMIN_TOKEN>

Response: 200 OK
{
  "success": true,
  "message": "User has 6 accessible pages",
  "data": [
    "employee_dashboard",
    "profile",
    "my_leave",
    "my_attendance",
    "my_payslip",
    "salary_management"  ← Custom grant
  ]
}
```

### 4. Get All Pages with Access Status
```
GET /api/page-access/user/{userId}/all-pages
Authorization: Bearer <ADMIN_TOKEN>

Response: 200 OK
{
  "success": true,
  "message": "All pages with access status",
  "data": [
    { "pageId": "employee_dashboard", "displayName": "Employee Dashboard", "hasAccess": true },
    { "pageId": "profile", "displayName": "My Profile", "hasAccess": true },
    { "pageId": "my_leave", "displayName": "My Leave", "hasAccess": true },
    { "pageId": "my_attendance", "displayName": "My Attendance", "hasAccess": true },
    { "pageId": "my_payslip", "displayName": "My Payslip", "hasAccess": false },  ← Revoked
    { "pageId": "salary_management", "displayName": "Salary Management", "hasAccess": true },  ← Granted
    { "pageId": "hr_dashboard", "displayName": "HR Dashboard", "hasAccess": false },
    ...
  ]
}
```

### 5. Get Custom Access Records
```
GET /api/page-access/user/{userId}/custom
Authorization: Bearer <ADMIN_TOKEN>

Response: 200 OK
{
  "success": true,
  "message": "Retrieved 2 custom access records",
  "data": [
    {
      "pageId": "salary_management",
      "pageName": "Salary Management",
      "granted": true,
      "grantedBy": "admin@company.com",
      "reason": "Special access for quarterly review",
      "createdAt": "2026-02-24T10:30:00"
    },
    {
      "pageId": "my_payslip",
      "pageName": "My Payslip",
      "granted": false,
      "grantedBy": "admin@company.com",
      "reason": "Temporary restriction during investigation",
      "createdAt": "2026-02-24T10:32:00"
    }
  ]
}
```

### 6. Batch Grant Pages
```
POST /api/page-access/grant-batch
Authorization: Bearer <ADMIN_TOKEN>

{
  "userId": "user-123",
  "pageIds": ["salary_management", "reports", "attendance"],
  "reason": "Promoted to supervisor"
}

Response: 200 OK
```

---

## Who Can Manage Page Access

Only these roles can grant/revoke access:
- ✅ **SUPER_ADMIN** - Can manage all enterprises
- ✅ **CEO** - Can manage own enterprise (HR, Employees)
- ✅ **ADMIN_HR** - Can manage own enterprise (HR, Employees)

Cannot manage:
- ❌ **HR** - Cannot manage others
- ❌ **MANAGER** - Cannot manage page access
- ❌ **EMPLOYEE** - Cannot manage page access

---

## Who Can Be Managed

Can have access modified:
- ✅ **HR** - Can be granted/revoked pages
- ✅ **EMPLOYEE** - Can be granted/revoked pages
- ✅ **MANAGER** - Can be granted/revoked pages

Cannot have access modified:
- ❌ **CEO** - Cannot be managed by other admins
- ❌ **ADMIN_HR** - Cannot be managed by CEOs
- ❌ **SUPER_ADMIN** - Cannot be managed by anyone

---

## Use Cases

### Use Case 1: Temporary Special Access
```
Scenario: Employee needs salary access for one project

Action:
1. Admin grants: salary_management page
2. Reason: "Contract negotiation access"
3. Employee can now see salary page

Later:
4. Admin revokes: salary_management page
5. Reason: "Project complete"
6. Employee loses access
```

### Use Case 2: Restrict Employee Access
```
Scenario: HR needs to prevent employee from seeing their payslip

Action:
1. Admin revokes: my_payslip page
2. Reason: "Payroll dispute - under review"
3. Employee cannot see payslip

After resolution:
4. Admin grants: my_payslip page
5. Reason: "Dispute resolved"
```

### Use Case 3: Promote Employee
```
Scenario: Employee promoted to supervisor/manager

Action:
1. Update role: EMPLOYEE → MANAGER (or keep EMPLOYEE, use grants)
2. Grant: manager_dashboard, team_management
3. New employee now has manager access

Or if keeping role:
- Just grant additional pages without role change
```

### Use Case 4: Onboarding New HR
```
Scenario: New HR specialist needs restricted access while training

Action:
1. Create user as HR (gets 11 default pages)
2. Revoke: salary_management (too sensitive for new hire)
3. Reason: "Training period - limited access"
4. Employee starts with 10 pages

After training:
5. Grant: salary_management
```

---

## Database Schema

### UserPageAccess Table
```sql
CREATE TABLE user_page_access (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    page_id VARCHAR(100) NOT NULL,
    granted BOOLEAN NOT NULL DEFAULT true,
    granted_by_id VARCHAR(36) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    
    UNIQUE KEY unique_user_page (user_id, page_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (granted_by_id) REFERENCES users(id)
);
```

---

## API Response Examples

### Grant Success
```json
{
  "success": true,
  "message": "Page access granted successfully",
  "data": {
    "id": "access-123",
    "userId": "user-456",
    "userName": "John Doe",
    "pageId": "salary_management",
    "pageName": "Salary Management",
    "granted": true,
    "grantedBy": "admin@company.com",
    "reason": "Special project access",
    "createdAt": "2026-02-24T10:30:00",
    "modifiedAt": "2026-02-24T10:30:00"
  }
}
```

### Error: Insufficient Permissions
```json
{
  "success": false,
  "message": "You do not have permission to manage page access",
  "errorCode": "PERMISSION_DENIED"
}
```

### Error: Cannot Manage Admin
```json
{
  "success": false,
  "message": "Cannot manage access for admin users",
  "errorCode": "INVALID_REQUEST"
}
```

### Error: Wrong Enterprise
```json
{
  "success": false,
  "message": "Cannot manage user from different enterprise",
  "errorCode": "INVALID_REQUEST"
}
```

---

## Audit Trail

Every grant/revoke creates a record with:
- ✅ Who made the change (grantedBy)
- ✅ When it was made (createdAt)
- ✅ Why it was made (reason)
- ✅ What changed (granted true/false)

**Example Audit Report:**
```
2026-02-24 10:30:00 | admin@company.com | Granted salary_management to john@company.com | Reason: Special project access
2026-02-24 10:32:00 | admin@company.com | Revoked my_payslip from john@company.com | Reason: Payroll dispute investigation
2026-02-24 14:15:00 | admin@company.com | Granted my_payslip to john@company.com | Reason: Dispute resolved
```

---

## Frontend Integration

### Display Current Access
```javascript
// Get all pages with access status
fetch('/api/page-access/user/{userId}/all-pages', {
  headers: { 'Authorization': 'Bearer ' + token }
})
.then(r => r.json())
.then(data => {
  // data.data has all pages with hasAccess boolean
  data.data.forEach(page => {
    if (page.hasAccess) {
      // Show page in admin panel
    }
  });
});
```

### Grant Access UI
```javascript
// Admin clicks "Grant salary_management"
fetch('/api/page-access/grant', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: selectedUserId,
    pageId: 'salary_management',
    action: 'grant',
    reason: 'Special project access'
  })
})
.then(r => r.json())
.then(data => {
  if (data.success) {
    alert('Access granted');
    refreshAccessStatus();
  }
});
```

---

## Complete Feature List

✅ Grant page access to users
✅ Revoke page access from users
✅ View all accessible pages (with custom overrides)
✅ View all pages with access status
✅ View custom access records
✅ Batch grant multiple pages
✅ Audit trail for all changes
✅ Enterprise isolation
✅ Permission checks (admin only)
✅ Reason logging

---

## Limitations

- Cannot manage SUPER_ADMIN users
- Cannot manage CEO users (from other roles)
- Cannot manage ADMIN_HR users (from CEO level)
- Can only manage own enterprise users
- Custom access overrides default role-based access

---

## Future Enhancements

- Schedule access (grant until date)
- Access templates (collections of pages)
- Bulk operations (manage many users at once)
- Analytics (track access usage)
- Approval workflow (request access, admin approves)

---

## Summary

Admins now have **complete control** over page access:
- ✅ Grant additional pages beyond role defaults
- ✅ Revoke pages from role defaults
- ✅ Manage multiple users and pages
- ✅ Track all changes with audit trail
- ✅ Maintain enterprise isolation

**Production-ready feature!** 🚀

