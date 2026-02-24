# 🏢 Multi-Tenant User Service - Production Implementation

## Overview

A **production-level User Service** that supports:
- ✅ Multi-tenancy (Multiple Companies/Enterprises)
- ✅ Role-based hierarchy (Admin > HR > Employee > Manager)
- ✅ JWT Authentication with BCrypt password encoding
- ✅ Manual Login & Secure Token Generation
- ✅ User Management with Role-Based Access Control (RBAC)
- ✅ Account Locking after failed login attempts
- ✅ Email Verification support
- ✅ Password Management & Change Password

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Auth Service                             │
│  (Calls User Service for authentication & authorization)    │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ HTTP Requests
                 │
         ┌───────▼──────────────────┐
         │   User Service (8081)    │
         └───────┬──────────────────┘
                 │
         ┌───────┴────────┬─────────────────┐
         │                │                 │
    ┌────▼────┐   ┌──────▼────┐   ┌────────▼────┐
    │  Auth   │   │   Users   │   │ Enterprises │
    │Controller   │ Controller   │  Controller  │
    └────┬────┘   └──────┬────┘   └────────┬────┘
         │                │                 │
    ┌────▼──────────────────────────────────▼────┐
    │      Security Layer (JWT + BCrypt)        │
    └────┬──────────────────────────────────────┬┘
         │                                      │
    ┌────▼────┐                        ┌───────▼────┐
    │  JPA    │◄──────────────────────►│  H2 DB    │
    │ Entities │                        │(Development)
    └─────────┘                         └────────────┘
         │
    ┌────▼────────────────────────────────────┐
    │  User, Enterprise, Role Hierarchy       │
    │  With Enterprise Isolation              │
    └─────────────────────────────────────────┘
```

---

## Key Features

### 1. Multi-Tenancy Support
- **Enterprise Entity**: Represents each company/tenant
- **Enterprise Isolation**: Each user is tied to an enterprise
- **Unique Constraints**: Email is unique per enterprise, not globally

```java
// User belongs to an Enterprise
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "enterprise_id", nullable = false)
private Enterprise enterprise;

// Queries are automatically scoped to enterprise
Optional<User> findByEmailAndEnterpriseId(String email, String enterpriseId);
```

### 2. Role-Based Hierarchy

| Role | Level | Permissions | Use Case |
|------|-------|-------------|----------|
| ADMIN | 0 | Full system access, create enterprises | System administrator |
| ADMIN_HR | 1 | Manage users in enterprise, HR functions | HR Department Head |
| HR | 2 | Create/update employees, manage team | HR Specialist |
| MANAGER | 3 | View team reports, manage direct reports | Department/Team Manager |
| EMPLOYEE | 4 | Basic access, view own profile | Regular employee |

```java
public enum UserRole {
    ADMIN("admin", "System Administrator"),
    ADMIN_HR("admin_hr", "HR Administrator"),
    HR("hr", "Human Resources"),
    EMPLOYEE("employee", "Employee"),
    MANAGER("manager", "Manager");
}
```

### 3. JWT Authentication

**Token Generation:**
```
Login Request → Verify Password → Generate JWT + Refresh Token → Return
```

**Token Structure:**
```json
{
  "sub": "user@example.com",
  "enterpriseId": "ent-001",
  "role": "ROLE_HR",
  "iat": 1677000000,
  "exp": 1677086400
}
```

**Usage:**
```
Authorization: Bearer eyJhbGc...
```

### 4. Password Security

- **BCrypt Encoding**: All passwords hashed with BCrypt (salt: 10 rounds)
- **Account Locking**: After 5 failed login attempts
- **Lock Duration**: 15 minutes
- **Password History**: Last password change tracked

```java
// Login attempt
if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
    if (user.getFailedLoginAttempts() >= 5) {
        user.lockAccount(); // 15 minutes
    }
}
```

### 5. User Hierarchy

**Manager-Employee Relationship:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "manager_id")
private User manager; // For role hierarchy

// Get all reports of a manager
List<User> getManagerReports(String managerId, String enterpriseId);
```

---

## API Endpoints

### Authentication Endpoints

#### 1. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "enterpriseId": "ent-001"
}

Response (200):
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "user-id",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "hr",
    "enterpriseId": "ent-001",
    "enterpriseName": "ACME Corp",
    "emailVerified": true
  }
}
```

#### 2. Create User (Admin/HR)
```http
POST /api/auth/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "newuser@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "employeeId": "EMP-001",
  "password": "SecurePass123",
  "department": "HR",
  "designation": "HR Executive",
  "phoneNumber": "+1234567890",
  "role": "HR",
  "managerId": "manager-id",
  "enterpriseId": "ent-001"
}
```

#### 3. Verify Email
```http
POST /api/auth/verify-email/{userId}
Authorization: Bearer <token>
```

#### 4. Change Password
```http
POST /api/auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "user-id",
  "oldPassword": "oldPassword",
  "newPassword": "newSecurePassword",
  "confirmPassword": "newSecurePassword"
}
```

### User Management Endpoints

#### 1. Get User by ID
```http
GET /api/users/{userId}
Authorization: Bearer <token>
```

#### 2. Get User by Email
```http
GET /api/users/email/{email}?enterpriseId={enterpriseId}
Authorization: Bearer <token>
```

#### 3. Get All Users in Enterprise
```http
GET /api/users/enterprise/{enterpriseId}
Authorization: Bearer <token>
(Requires: ADMIN, ADMIN_HR, HR role)
```

#### 4. Get Users by Role
```http
GET /api/users/enterprise/{enterpriseId}/role/{role}
Authorization: Bearer <token>
(Requires: ADMIN, ADMIN_HR, HR role)

Example: /api/users/enterprise/ent-001/role/employee
```

#### 5. Get HR Users
```http
GET /api/users/enterprise/{enterpriseId}/hr
Authorization: Bearer <token>
(Requires: ADMIN, ADMIN_HR role)
```

#### 6. Get Manager's Reports
```http
GET /api/users/{managerId}/reports?enterpriseId={enterpriseId}
Authorization: Bearer <token>
(Requires: ADMIN, ADMIN_HR, MANAGER role)
```

#### 7. Update User
```http
PUT /api/users/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Updated",
  "lastName": "Name",
  "department": "IT",
  "designation": "Senior Developer",
  "phoneNumber": "+9876543210",
  "profileImageUrl": "https://..."
}
```

#### 8. Deactivate User
```http
DELETE /api/users/{userId}
Authorization: Bearer <token>
(Requires: ADMIN, ADMIN_HR role)
```

### Enterprise Management Endpoints

#### 1. Create Enterprise
```http
POST /api/enterprises
Authorization: Bearer <token>
(Requires: ADMIN role)

{
  "name": "ACME Corporation",
  "code": "ACME-CORP",
  "description": "Leading technology company",
  "email": "admin@acme.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main St",
  "city": "New York",
  "country": "USA",
  "zipCode": "10001"
}
```

#### 2. Get Enterprise
```http
GET /api/enterprises/{id}
Authorization: Bearer <token>
```

#### 3. Get Enterprise by Code
```http
GET /api/enterprises/code/{code}
Authorization: Bearer <token>
```

#### 4. Get All Enterprises
```http
GET /api/enterprises
Authorization: Bearer <token>
(Requires: ADMIN role)
```

#### 5. Update Enterprise
```http
PUT /api/enterprises/{id}
Authorization: Bearer <token>
(Requires: ADMIN role)
```

#### 6. Deactivate Enterprise
```http
DELETE /api/enterprises/{id}
Authorization: Bearer <token>
(Requires: ADMIN role)
```

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'ADMIN_HR', 'HR', 'EMPLOYEE', 'MANAGER'),
    department VARCHAR(100),
    designation VARCHAR(100),
    phone_number VARCHAR(20),
    manager_id VARCHAR(36),
    active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    email_verification_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_login TIMESTAMP,
    last_password_change_date TIMESTAMP,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP,
    profile_image_url VARCHAR(255),
    
    UNIQUE KEY unique_email_enterprise (email, enterprise_id),
    UNIQUE KEY unique_employee_enterprise (employee_id, enterprise_id),
    FOREIGN KEY (enterprise_id) REFERENCES enterprises(id),
    FOREIGN KEY (manager_id) REFERENCES users(id)
);
```

### Enterprises Table
```sql
CREATE TABLE enterprises (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(1000),
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## Configuration

### JWT Configuration (application.properties)
```properties
jwt.secret-key=MyVeryLongSecretKeyForJWTSigningThatIsAtLeast256BitsLongForHS256Algorithm12345
jwt.expiration=86400000          # 24 hours in ms
jwt.refresh-token-expiration=604800000  # 7 days in ms
```

### Database Configuration
```properties
spring.datasource.url=jdbc:h2:mem:userdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

### Security Configuration
```properties
# CORS & CSRF disabled (for API-only service)
# JWT-based stateless authentication
# Method-level security with @PreAuthorize
```

---

## Security Best Practices Implemented

1. ✅ **Password Encoding**: BCrypt with 10 salt rounds
2. ✅ **JWT Tokens**: Stateless authentication with expiration
3. ✅ **Account Locking**: After 5 failed attempts for 15 minutes
4. ✅ **Role-Based Access Control**: Method-level authorization with @PreAuthorize
5. ✅ **Enterprise Isolation**: Multi-tenant data segregation
6. ✅ **Email Uniqueness**: Per enterprise (not globally unique)
7. ✅ **Exception Handling**: Secure error messages without sensitive data
8. ✅ **Logging**: Audit trail for security events

---

## Usage Example

### 1. Create Enterprise
```bash
curl -X POST http://localhost:8081/api/enterprises \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "name": "ACME Corp",
    "code": "ACME-CORP",
    "email": "admin@acme.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "country": "USA",
    "zipCode": "10001"
  }'
```

### 2. Create First Admin User
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@acme.com",
    "firstName": "System",
    "lastName": "Admin",
    "employeeId": "ADM-001",
    "password": "AdminPass123!",
    "phoneNumber": "+1234567890",
    "role": "ADMIN",
    "enterpriseId": "ent-123"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@acme.com",
    "password": "AdminPass123!",
    "enterpriseId": "ent-123"
  }'
```

### 4. Create HR User
```bash
curl -X POST http://localhost:8081/api/auth/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "email": "hr@acme.com",
    "firstName": "HR",
    "lastName": "Manager",
    "employeeId": "HR-001",
    "password": "HRPass123!",
    "department": "HR",
    "designation": "HR Manager",
    "phoneNumber": "+9876543210",
    "role": "ADMIN_HR",
    "enterpriseId": "ent-123"
  }'
```

---

## Error Handling

All errors return consistent JSON responses:

```json
{
  "success": false,
  "message": "Descriptive error message",
  "errorCode": "ERROR_CODE",
  "timestamp": "2026-02-24T10:30:00"
}
```

### Common Error Codes
- `VALIDATION_ERROR`: Request validation failed
- `INVALID_CREDENTIALS`: Email/password incorrect
- `AUTH_FAILED`: Authentication failed
- `ACCOUNT_LOCKED`: Account locked after failed attempts
- `USER_NOT_FOUND`: User doesn't exist
- `ENTERPRISE_NOT_FOUND`: Enterprise doesn't exist
- `INVALID_ARGUMENT`: Invalid argument provided
- `INTERNAL_ERROR`: Unexpected server error

---

## Deployment Considerations

1. **Database**: Use PostgreSQL/MySQL in production instead of H2
2. **JWT Secret**: Use strong, environment-specific secret key
3. **HTTPS**: Always use HTTPS in production
4. **Rate Limiting**: Implement rate limiting on login endpoint
5. **Monitoring**: Log all authentication attempts
6. **Backup**: Regular database backups
7. **Scaling**: Use horizontal scaling with load balancers
8. **Email Service**: Integrate for email verification

---

## Testing the System

See `TESTING_GUIDE.md` for comprehensive testing scenarios

