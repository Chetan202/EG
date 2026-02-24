# Testing Enterprise Hierarchy - Complete Guide

## Test Scenario 1: SUPER_ADMIN Creates CEO

### Step 1: Create First Enterprise
```bash
POST http://localhost:8081/api/enterprises
Content-Type: application/json

{
  "name": "TechCorp Inc",
  "code": "TECHCORP",
  "description": "Leading Technology Company",
  "email": "admin@techcorp.com",
  "phoneNumber": "+1-800-TECH",
  "address": "100 Tech Drive",
  "city": "San Francisco",
  "country": "USA",
  "zipCode": "94105"
}

Response: 201 Created
{
  "id": "ent-001",
  "name": "TechCorp Inc",
  ...
}

Save: ENTERPRISE_ID = "ent-001"
```

### Step 2: Create SUPER_ADMIN User
```bash
POST http://localhost:8081/api/auth/users
Content-Type: application/json

{
  "email": "superadmin@system.com",
  "firstName": "System",
  "lastName": "Administrator",
  "employeeId": "SUPER-001",
  "password": "SuperAdminPass@123",
  "role": "SUPER_ADMIN",
  "enterpriseId": "ent-001"
}

Response: 201 Created
```

### Step 3: SUPER_ADMIN Login
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "superadmin@system.com",
  "password": "SuperAdminPass@123",
  "enterpriseId": "ent-001"
}

Response: 200 OK
{
  "accessToken": "eyJhbGc...",
  "user": {
    "role": "super_admin",
    ...
  }
}

Save: SUPER_ADMIN_TOKEN = token value
```

### Step 4: SUPER_ADMIN Creates CEO
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <SUPER_ADMIN_TOKEN>
Content-Type: application/json

{
  "email": "ceo@techcorp.com",
  "firstName": "John",
  "lastName": "CEO",
  "employeeId": "CEO-001",
  "password": "CEOPass@123",
  "role": "CEO",
  "enterpriseId": "ent-001"
}

Response: 201 Created ✓
Message: "User created successfully"
```

---

## Test Scenario 2: CEO Creates HR (Should Work)

### Step 1: CEO Login
```bash
POST http://localhost:8081/api/auth/login

{
  "email": "ceo@techcorp.com",
  "password": "CEOPass@123",
  "enterpriseId": "ent-001"
}

Response: 200 OK
Save: CEO_TOKEN = token
```

### Step 2: CEO Creates HR User ✓ (Should Succeed)
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <CEO_TOKEN>

{
  "email": "hr@techcorp.com",
  "firstName": "Sarah",
  "lastName": "HR-Manager",
  "employeeId": "HR-001",
  "password": "HRPass@123",
  "role": "HR",
  "enterpriseId": "ent-001"
}

Response: 201 Created ✓
Message: "User created successfully"
```

---

## Test Scenario 3: CEO Tries to Create EMPLOYEE (Should Fail)

### Step 1: CEO Attempts to Create Employee
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <CEO_TOKEN>

{
  "email": "emp@techcorp.com",
  "firstName": "Bob",
  "lastName": "Developer",
  "employeeId": "EMP-001",
  "password": "EmpPass@123",
  "role": "EMPLOYEE",
  "enterpriseId": "ent-001"
}

Response: 403 Forbidden ✗
Message: "You do not have permission to create user with role: employee"
```

✅ **TEST PASSED** - CEO cannot create employees!

---

## Test Scenario 4: HR Creates EMPLOYEE (Should Work)

### Step 1: HR Login
```bash
POST http://localhost:8081/api/auth/login

{
  "email": "hr@techcorp.com",
  "password": "HRPass@123",
  "enterpriseId": "ent-001"
}

Response: 200 OK
Save: HR_TOKEN = token
```

### Step 2: HR Creates Employee ✓
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <HR_TOKEN>

{
  "email": "emp@techcorp.com",
  "firstName": "Bob",
  "lastName": "Developer",
  "employeeId": "EMP-001",
  "password": "EmpPass@123",
  "role": "EMPLOYEE",
  "enterpriseId": "ent-001"
}

Response: 201 Created ✓
Message: "User created successfully"
```

✅ **TEST PASSED** - HR can create employees!

---

## Test Scenario 5: EMPLOYEE Creates User (Should Fail)

### Step 1: Employee Login
```bash
POST http://localhost:8081/api/auth/login

{
  "email": "emp@techcorp.com",
  "password": "EmpPass@123",
  "enterpriseId": "ent-001"
}

Response: 200 OK
Save: EMPLOYEE_TOKEN = token
```

### Step 2: Employee Attempts to Create User
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <EMPLOYEE_TOKEN>

{
  "email": "emp2@techcorp.com",
  "firstName": "Jane",
  "lastName": "Developer2",
  "employeeId": "EMP-002",
  "password": "Pass@123",
  "role": "EMPLOYEE",
  "enterpriseId": "ent-001"
}

Response: 403 Forbidden ✗
Message: "Access Denied"
```

✅ **TEST PASSED** - Employee cannot create users!

---

## Test Scenario 6: Page Access Control

### Step 1: Get All Accessible Pages for Employee
```bash
GET http://localhost:8081/api/pages/accessible
Authorization: Bearer <EMPLOYEE_TOKEN>

Response: 200 OK
{
  "success": true,
  "data": [
    { "pageId": "employee_dashboard", "displayName": "Employee Dashboard" },
    { "pageId": "profile", "displayName": "My Profile" },
    { "pageId": "my_leave", "displayName": "My Leave" },
    { "pageId": "my_attendance", "displayName": "My Attendance" },
    { "pageId": "my_payslip", "displayName": "My Payslip" }
  ]
}

Count: 5 pages
```

✅ **TEST PASSED** - Employee sees only 5 pages!

### Step 2: Get All Accessible Pages for HR
```bash
GET http://localhost:8081/api/pages/accessible
Authorization: Bearer <HR_TOKEN>

Response: 200 OK
{
  "success": true,
  "data": [
    { "pageId": "hr_dashboard", "displayName": "HR Dashboard" },
    { "pageId": "employee_management", "displayName": "Employee Management" },
    { "pageId": "employee_records", "displayName": "Employee Records" },
    { "pageId": "attendance", "displayName": "Attendance Management" },
    { "pageId": "leave_management", "displayName": "Leave Management" },
    { "pageId": "reports", "displayName": "HR Reports" },
    // ... plus all employee pages
  ]
}

Count: 11+ pages
```

✅ **TEST PASSED** - HR sees more pages!

### Step 3: Get Pages for CEO
```bash
GET http://localhost:8081/api/pages/accessible
Authorization: Bearer <CEO_TOKEN>

Response: 200 OK
Count: 14+ pages (including Enterprise Dashboard, Settings, Billing)
```

✅ **TEST PASSED** - CEO sees enterprise pages!

---

## Test Scenario 7: Page Access Check

### Step 1: Employee Checks System Admin Page
```bash
GET http://localhost:8081/api/pages/check/system_admin
Authorization: Bearer <EMPLOYEE_TOKEN>

Response: 200 OK
{
  "success": true,
  "message": "Access denied",
  "data": false
}
```

✅ **TEST PASSED** - Employee denied access!

### Step 2: CEO Checks Salary Management Page
```bash
GET http://localhost:8081/api/pages/check/salary_management
Authorization: Bearer <CEO_TOKEN>

Response: 200 OK
{
  "success": true,
  "message": "Access granted",
  "data": true
}
```

✅ **TEST PASSED** - CEO granted access!

### Step 3: Employee Checks My Payslip Page
```bash
GET http://localhost:8081/api/pages/check/my_payslip
Authorization: Bearer <EMPLOYEE_TOKEN>

Response: 200 OK
{
  "success": true,
  "message": "Access granted",
  "data": true
}
```

✅ **TEST PASSED** - Employee granted access!

---

## Test Scenario 8: Get Role Information

### Step 1: Get CEO Role Info
```bash
GET http://localhost:8081/api/pages/role-info/ceo
Authorization: Bearer <SUPER_ADMIN_TOKEN>

Response: 200 OK
{
  "success": true,
  "data": {
    "roleCode": "ceo",
    "roleName": "Chief Executive Officer",
    "permissions": "Enterprise head, can create HR",
    "canManageEnterprises": false,
    "canManageHR": true,
    "canManageEmployees": false,
    "canAccessPages": true,
    "totalAccessiblePages": 14,
    "accessiblePageIds": [
      "enterprise_dashboard",
      "enterprise_settings",
      "billing_management",
      "hr_dashboard",
      ...
    ]
  }
}
```

✅ **TEST PASSED** - Role info retrieved!

---

## Test Scenario 9: Deactivate User with Permission Check

### Step 1: CEO Tries to Deactivate Another CEO (Should Fail)
```bash
DELETE http://localhost:8081/api/users/<other-ceo-id>
Authorization: Bearer <CEO_TOKEN>

Response: 403 Forbidden
Message: "You do not have permission to deactivate this user"
```

✅ **TEST PASSED** - CEO cannot deactivate another CEO!

### Step 2: CEO Deactivates HR User (Should Succeed)
```bash
DELETE http://localhost:8081/api/users/<hr-id>
Authorization: Bearer <CEO_TOKEN>

Response: 204 No Content ✓
```

✅ **TEST PASSED** - CEO can deactivate HR!

### Step 3: HR Deactivates Employee (Should Succeed)
```bash
DELETE http://localhost:8081/api/users/<emp-id>
Authorization: Bearer <HR_TOKEN>

Response: 204 No Content ✓
```

✅ **TEST PASSED** - HR can deactivate employee!

### Step 4: Employee Tries to Deactivate Anyone (Should Fail)
```bash
DELETE http://localhost:8081/api/users/<any-user-id>
Authorization: Bearer <EMPLOYEE_TOKEN>

Response: 403 Forbidden
Message: "Access Denied"
```

✅ **TEST PASSED** - Employee cannot deactivate anyone!

---

## Test Scenario 10: Multi-Tenancy Isolation

### Step 1: Create Second Enterprise
```bash
POST http://localhost:8081/api/enterprises
Authorization: Bearer <SUPER_ADMIN_TOKEN>

{
  "name": "FinCorp",
  "code": "FINCORP",
  ...
}

Response: 201 Created
Save: ENTERPRISE_ID_2 = "ent-002"
```

### Step 2: SUPER_ADMIN Creates CEO in Enterprise 2
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <SUPER_ADMIN_TOKEN>

{
  "email": "ceo2@fincorp.com",
  "firstName": "Jane",
  "lastName": "CEO2",
  "employeeId": "CEO-002",
  "password": "CEO2Pass@123",
  "role": "CEO",
  "enterpriseId": "ent-002"
}

Response: 201 Created
```

### Step 3: CEO 1 Tries to Create User in Enterprise 2 (Should Fail)
```bash
POST http://localhost:8081/api/auth/users
Authorization: Bearer <CEO_TOKEN>

{
  "email": "hr2@fincorp.com",
  "firstName": "Bob",
  "lastName": "HR",
  "employeeId": "HR-002",
  "password": "Pass@123",
  "role": "HR",
  "enterpriseId": "ent-002"  ← Different enterprise!
}

Response: 403 Forbidden
Message: "You do not have permission to create user..."
```

✅ **TEST PASSED** - Multi-tenancy isolation enforced!

---

## Complete Test Checklist

- [ ] SUPER_ADMIN creates CEO ✓
- [ ] CEO creates HR ✓
- [ ] CEO CANNOT create EMPLOYEE ✓
- [ ] HR creates EMPLOYEE ✓
- [ ] EMPLOYEE cannot create anyone ✓
- [ ] Employee sees only employee pages ✓
- [ ] HR sees HR + employee pages ✓
- [ ] CEO sees enterprise + HR pages ✓
- [ ] SUPER_ADMIN sees all pages ✓
- [ ] Page access check works ✓
- [ ] Role info retrieved correctly ✓
- [ ] CEO cannot deactivate CEO ✓
- [ ] CEO can deactivate HR ✓
- [ ] HR can deactivate EMPLOYEE ✓
- [ ] EMPLOYEE cannot deactivate anyone ✓
- [ ] Multi-tenancy isolation enforced ✓

**All tests must PASS for production deployment!** ✅

---

## Troubleshooting

### Issue: "Access Denied" instead of specific permission error
**Solution**: Check that user has proper role (needs CEO/ADMIN_HR/HR to create)

### Issue: Page access returns empty list
**Solution**: Login with user that has page access (try HR or CEO)

### Issue: Multi-tenancy test fails
**Solution**: Ensure CEO's enterpriseId matches the one they're trying to manage

### Issue: Deactivate fails
**Solution**: Check actor role - needs CEO/ADMIN_HR level permissions

---

**Ready for integration with web frontend!** 🎉

