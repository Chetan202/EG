# Testing Guide - User Service

## Quick Start

### 1. Start the Application
```bash
cd User-Service
mvn spring-boot:run
```

Server runs on: `http://localhost:8081`

### 2. Access H2 Console (Development)
```
http://localhost:8081/h2-console
JDBC URL: jdbc:h2:mem:userdb
Username: sa
Password: (leave blank)
```

---

## Test Scenarios

### Scenario 1: Create Enterprise

**Step 1: Create Enterprise**
```bash
curl -X POST http://localhost:8081/api/enterprises \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Innovations Inc",
    "code": "TECH-INC",
    "description": "Leading tech company",
    "email": "admin@techinc.com",
    "phoneNumber": "+1-800-TECH-INC",
    "address": "456 Tech Avenue",
    "city": "San Francisco",
    "country": "USA",
    "zipCode": "94105"
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tech Innovations Inc",
  "code": "TECH-INC",
  ...
}
```

Save the `id` as `ENTERPRISE_ID` for further steps.

---

### Scenario 2: User Authentication Flow

**Step 1: Create Admin User**
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techinc.com",
    "firstName": "John",
    "lastName": "Admin",
    "employeeId": "EMP-0001",
    "password": "AdminPass@123",
    "department": "Administration",
    "designation": "System Administrator",
    "phoneNumber": "+1-800-ADMIN",
    "role": "ADMIN",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Response:**
```json
{
  "id": "user-001",
  "email": "admin@techinc.com",
  "firstName": "John",
  "lastName": "Admin",
  "employeeId": "EMP-0001",
  "role": "ADMIN",
  ...
}
```

**Step 2: Login**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techinc.com",
    "password": "AdminPass@123",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "user-001",
    "email": "admin@techinc.com",
    "fullName": "John Admin",
    "role": "admin",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000",
    "enterpriseName": "Tech Innovations Inc",
    "emailVerified": false
  }
}
```

Save the `accessToken` for authenticated requests.

**Step 3: Verify Email**
```bash
curl -X POST http://localhost:8081/api/auth/verify-email/user-001 \
  -H "Authorization: Bearer <accessToken>"
```

---

### Scenario 3: Create HR Users

**Step 1: Create HR Admin (with admin token)**
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <adminToken>" \
  -d '{
    "email": "hr-admin@techinc.com",
    "firstName": "Sarah",
    "lastName": "HR-Lead",
    "employeeId": "EMP-0002",
    "password": "HRAdmin@123",
    "department": "Human Resources",
    "designation": "HR Manager",
    "phoneNumber": "+1-800-HR-ADMIN",
    "role": "ADMIN_HR",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Step 2: Create Regular HR User**
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <hrAdminToken>" \
  -d '{
    "email": "hr-specialist@techinc.com",
    "firstName": "Mike",
    "lastName": "HR-Specialist",
    "employeeId": "EMP-0003",
    "password": "HRSpec@123",
    "department": "Human Resources",
    "designation": "HR Specialist",
    "phoneNumber": "+1-800-HR-SPEC",
    "role": "HR",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### Scenario 4: Create Employee with Manager Hierarchy

**Step 1: Create Manager**
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <hrAdminToken>" \
  -d '{
    "email": "manager@techinc.com",
    "firstName": "Alice",
    "lastName": "Manager",
    "employeeId": "EMP-0100",
    "password": "Manager@123",
    "department": "Engineering",
    "designation": "Engineering Manager",
    "phoneNumber": "+1-800-MANAGER",
    "role": "MANAGER",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

Save the manager's `id` as `MANAGER_ID`.

**Step 2: Create Employee under Manager**
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <hrAdminToken>" \
  -d '{
    "email": "developer@techinc.com",
    "firstName": "Bob",
    "lastName": "Developer",
    "employeeId": "EMP-0101",
    "password": "Developer@123",
    "department": "Engineering",
    "designation": "Senior Developer",
    "phoneNumber": "+1-800-DEVELOPER",
    "role": "EMPLOYEE",
    "managerId": "<MANAGER_ID>",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### Scenario 5: User Management Operations

**Get User by ID**
```bash
curl -X GET http://localhost:8081/api/users/user-001 \
  -H "Authorization: Bearer <token>"
```

**Get User by Email**
```bash
curl -X GET "http://localhost:8081/api/users/email/admin@techinc.com?enterpriseId=550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer <token>"
```

**Get All Users in Enterprise (HR/Admin only)**
```bash
curl -X GET http://localhost:8081/api/users/enterprise/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <hrAdminToken>"
```

**Get Users by Role**
```bash
curl -X GET http://localhost:8081/api/users/enterprise/550e8400-e29b-41d4-a716-446655440000/role/employee \
  -H "Authorization: Bearer <hrAdminToken>"
```

**Get HR Users**
```bash
curl -X GET http://localhost:8081/api/users/enterprise/550e8400-e29b-41d4-a716-446655440000/hr \
  -H "Authorization: Bearer <adminToken>"
```

**Get Manager's Reports**
```bash
curl -X GET "http://localhost:8081/api/users/<MANAGER_ID>/reports?enterpriseId=550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer <managerToken>"
```

**Update User**
```bash
curl -X PUT http://localhost:8081/api/users/user-001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "John",
    "lastName": "AdminUpdated",
    "department": "Executive",
    "designation": "Chief Admin Officer",
    "phoneNumber": "+1-800-CHIEF-ADMIN",
    "profileImageUrl": "https://example.com/profile.jpg"
  }'
```

**Deactivate User (Admin/HR only)**
```bash
curl -X DELETE http://localhost:8081/api/users/user-001 \
  -H "Authorization: Bearer <hrAdminToken>"
```

---

### Scenario 6: Password Management

**Change Password**
```bash
curl -X POST http://localhost:8081/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": "user-001",
    "oldPassword": "AdminPass@123",
    "newPassword": "NewAdminPass@456",
    "confirmPassword": "NewAdminPass@456"
  }'
```

**Login with New Password**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techinc.com",
    "password": "NewAdminPass@456",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### Scenario 7: Failed Login & Account Locking

**Test 1: Wrong Password (attempt 1)**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techinc.com",
    "password": "WrongPassword",
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

Response: `401 Unauthorized - Invalid email or password`

**Test 2: Repeat 4 more times (total 5 attempts)**

**Test 3: 6th attempt (Account is now locked)**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techinc.com",
    "password": "AdminPass@123",  # Correct password
    "enterpriseId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

Response: `401 Unauthorized - Account locked due to multiple failed login attempts`

**Test 4: Wait 15+ minutes and try again** ✅ Login succeeds

---

### Scenario 8: Enterprise Management

**Get Enterprise by ID**
```bash
curl -X GET http://localhost:8081/api/enterprises/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <token>"
```

**Get Enterprise by Code**
```bash
curl -X GET http://localhost:8081/api/enterprises/code/TECH-INC \
  -H "Authorization: Bearer <token>"
```

**Get All Enterprises (Admin only)**
```bash
curl -X GET http://localhost:8081/api/enterprises \
  -H "Authorization: Bearer <adminToken>"
```

**Update Enterprise (Admin only)**
```bash
curl -X PUT http://localhost:8081/api/enterprises/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <adminToken>" \
  -d '{
    "name": "Tech Innovations Updated",
    "description": "Updated leading tech company",
    "phoneNumber": "+1-800-TECH-UPDATE"
  }'
```

**Deactivate Enterprise (Admin only)**
```bash
curl -X DELETE http://localhost:8081/api/enterprises/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <adminToken>"
```

---

## Role-Based Access Control Testing

| Endpoint | ADMIN | ADMIN_HR | HR | MANAGER | EMPLOYEE |
|----------|-------|----------|----|---------| ----------|
| POST /auth/users | ✅ | ✅ | ✅ | ❌ | ❌ |
| GET /users/enterprise/{id} | ✅ | ✅ | ✅ | ❌ | ❌ |
| GET /users/enterprise/{id}/hr | ✅ | ✅ | ❌ | ❌ | ❌ |
| GET /users/{id}/reports | ✅ | ✅ | ✅ | ✅ | ❌ |
| DELETE /users/{id} | ✅ | ✅ | ❌ | ❌ | ❌ |
| POST /enterprises | ✅ | ❌ | ❌ | ❌ | ❌ |
| DELETE /enterprises/{id} | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## Common Issues & Solutions

### Issue: "Could not autowire UserDetailsService"
**Solution**: Ensure `CustomUserDetailsService` is properly annotated with `@Service`

### Issue: JWT Token validation fails
**Solution**: Check that `JwtTokenProvider` bean is registered and JWT secret is configured

### Issue: Enterprise not found when creating user
**Solution**: Ensure enterprise exists first and use correct `enterpriseId`

### Issue: Email already exists error
**Solution**: Emails are unique per enterprise. Try a different email or use different enterprise.

---

## Performance Testing

### Load Testing: Create 1000 Users
```bash
for i in {1..1000}; do
  curl -X POST http://localhost:8081/api/auth/users \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer <token>" \
    -d "{
      \"email\": \"user$i@techinc.com\",
      \"firstName\": \"User\",
      \"lastName\": \"$i\",
      \"employeeId\": \"EMP-$i\",
      \"password\": \"Pass@123\",
      \"phoneNumber\": \"+1-800-USER$i\",
      \"role\": \"EMPLOYEE\",
      \"enterpriseId\": \"550e8400-e29b-41d4-a716-446655440000\"
    }"
done
```

### Query Performance: Get All Users
```bash
time curl -X GET http://localhost:8081/api/users/enterprise/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <token>"
```

---

## Verification Checklist

- [ ] Enterprise creation works
- [ ] User creation works with different roles
- [ ] Login successful with correct password
- [ ] Login fails with wrong password
- [ ] Account locks after 5 failed attempts
- [ ] Account unlocks after 15 minutes
- [ ] JWT token is valid
- [ ] Role-based access control works
- [ ] User hierarchy (manager-employee) works
- [ ] Email verification endpoint works
- [ ] Password change works
- [ ] User deactivation works
- [ ] Multi-tenancy isolation works (same email in different enterprises)
- [ ] Retrieve users by role works
- [ ] Get manager reports works
- [ ] All endpoints return proper error messages

