# Microservice Integration Guide - Enterprise Hierarchy

## Overview

Your **User Service** now provides:
- Enterprise-level role hierarchy
- Dynamic page access control
- Multi-tenant user management

Other microservices and frontend can integrate via REST APIs.

---

## For Frontend/Website Team

### 1. After User Logs In

```javascript
// Token received from login
const token = response.data.accessToken;

// Get user role
const userData = response.data.user;
const userRole = userData.role; // 'ceo', 'hr', 'employee', etc.
const enterpriseId = userData.enterpriseId;

// Store for future use
localStorage.setItem('token', token);
localStorage.setItem('userRole', userRole);
localStorage.setItem('enterpriseId', enterpriseId);
```

### 2. Build Dynamic Navigation

```javascript
// Fetch accessible pages for this user
async function loadNavigationMenu() {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8081/api/pages/accessible', {
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  
  const data = await response.json();
  
  // data.data contains:
  // [
  //   { pageId: 'employee_dashboard', displayName: 'Employee Dashboard' },
  //   { pageId: 'profile', displayName: 'My Profile' },
  //   ...
  // ]
  
  renderNavigation(data.data);
}

function renderNavigation(pages) {
  const navMenu = document.getElementById('nav-menu');
  navMenu.innerHTML = '';
  
  pages.forEach(page => {
    const link = document.createElement('a');
    link.href = `/pages/${page.pageId}`;
    link.textContent = page.displayName;
    navMenu.appendChild(link);
  });
}
```

### 3. Check Page Access Before Navigation

```javascript
async function canAccessPage(pageId) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(
    `http://localhost:8081/api/pages/check/${pageId}`,
    {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    }
  );
  
  const data = await response.json();
  return data.data; // true or false
}

// Usage when user clicks a link
document.addEventListener('click', async (e) => {
  if (e.target.tagName === 'A') {
    const pageId = e.target.href.split('/').pop();
    
    if (!await canAccessPage(pageId)) {
      e.preventDefault();
      alert('You do not have access to this page');
      return false;
    }
  }
});
```

### 4. Show Role-Specific Dashboards

```javascript
function loadDashboard() {
  const userRole = localStorage.getItem('userRole');
  
  switch(userRole) {
    case 'super_admin':
      loadSystemAdminDashboard();
      break;
    case 'ceo':
      loadEnterpriseDashboard();
      break;
    case 'admin_hr':
    case 'hr':
      loadHRDashboard();
      break;
    case 'manager':
      loadManagerDashboard();
      break;
    case 'employee':
      loadEmployeeDashboard();
      break;
  }
}
```

### 5. Conditional UI Elements

```javascript
// Hide elements based on role
function updateUIBasedOnRole() {
  const userRole = localStorage.getItem('userRole');
  
  // Hide salary management for HR
  if (userRole === 'hr') {
    document.getElementById('salary-section').style.display = 'none';
  }
  
  // Show team management for managers
  if (userRole === 'manager' || userRole === 'admin_hr' || userRole === 'ceo') {
    document.getElementById('team-management').style.display = 'block';
  }
  
  // Show system settings for admin
  if (userRole === 'super_admin') {
    document.getElementById('system-settings').style.display = 'block';
  }
}
```

---

## For Backend Services Integration

### 1. Create User with Role Validation

```python
# Python example (requests library)
import requests

def create_user(token, email, role, enterprise_id):
    url = 'http://localhost:8081/api/auth/users'
    headers = {'Authorization': f'Bearer {token}'}
    
    data = {
        'email': email,
        'firstName': 'John',
        'lastName': 'Doe',
        'employeeId': 'EMP-001',
        'password': 'SecurePass@123',
        'role': role,
        'enterpriseId': enterprise_id
    }
    
    response = requests.post(url, json=data, headers=headers)
    
    if response.status_code == 201:
        return response.json()  # User created
    elif response.status_code == 403:
        print(f"Permission denied: {response.json()['message']}")
    else:
        print(f"Error: {response.status_code}")
    
    return None

# Usage
admin_token = "..."
user = create_user(admin_token, 'ceo@company.com', 'CEO', 'ent-001')
```

### 2. Get User's Accessible Pages

```python
def get_accessible_pages(token):
    url = 'http://localhost:8081/api/pages/accessible'
    headers = {'Authorization': f'Bearer {token}'}
    
    response = requests.get(url, headers=headers)
    
    if response.status_code == 200:
        pages = response.json()['data']
        return [page['pageId'] for page in pages]
    
    return []
```

### 3. Check Role Permissions

```python
def can_access_page(token, page_id):
    url = f'http://localhost:8081/api/pages/check/{page_id}'
    headers = {'Authorization': f'Bearer {token}'}
    
    response = requests.get(url, headers=headers)
    
    if response.status_code == 200:
        return response.json()['data']  # true or false
    
    return False

def has_permission_to_manage(token, action, target_role):
    """
    Check if user can perform action on target role
    """
    # Get user from token
    # Check if user.role.canCreateRole(target_role)
    # This logic should be in your backend
    pass
```

---

## API Response Examples

### Get Accessible Pages Response

```json
{
  "success": true,
  "message": "Accessible pages retrieved",
  "data": [
    {
      "pageId": "employee_dashboard",
      "displayName": "Employee Dashboard",
      "allowedRoles": ["super_admin", "ceo", "admin_hr", "hr", "manager", "employee"]
    },
    {
      "pageId": "profile",
      "displayName": "My Profile",
      "allowedRoles": ["super_admin", "ceo", "admin_hr", "hr", "manager", "employee"]
    },
    {
      "pageId": "my_leave",
      "displayName": "My Leave",
      "allowedRoles": ["super_admin", "ceo", "admin_hr", "hr", "manager", "employee"]
    },
    {
      "pageId": "my_attendance",
      "displayName": "My Attendance",
      "allowedRoles": ["super_admin", "ceo", "admin_hr", "hr", "manager", "employee"]
    },
    {
      "pageId": "my_payslip",
      "displayName": "My Payslip",
      "allowedRoles": ["super_admin", "ceo", "admin_hr", "hr", "manager", "employee"]
    }
  ]
}
```

### Check Page Access Response

```json
{
  "success": true,
  "message": "Access denied",
  "data": false
}
```

### Get Role Info Response

```json
{
  "success": true,
  "message": "Role information retrieved",
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
      "employee_management",
      "employee_records",
      "salary_management",
      "attendance",
      "leave_management",
      "reports",
      "manager_dashboard",
      "team_management",
      "employee_dashboard",
      "profile"
    ]
  }
}
```

---

## Common Integration Patterns

### Pattern 1: Show/Hide Based on Role

```javascript
// After login
const userRole = loginResponse.user.role;

// Check role directly
if (userRole === 'super_admin') {
  // Show system admin controls
}

// Or fetch accessible pages
fetch('/api/pages/accessible')
  .then(r => r.json())
  .then(data => {
    const pageIds = data.data.map(p => p.pageId);
    if (pageIds.includes('salary_management')) {
      // Show salary controls
    }
  });
```

### Pattern 2: Permission Guard for Routes

```javascript
// React Router example
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ pageId, children }) {
  const [canAccess, setCanAccess] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    fetch(`/api/pages/check/${pageId}`, {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(r => r.json())
    .then(data => {
      setCanAccess(data.data);
      setLoading(false);
    });
  }, [pageId]);
  
  if (loading) return <Spinner />;
  if (!canAccess) return <AccessDenied />;
  
  return children;
}

// Usage
<Routes>
  <Route path="/employee-dashboard" element={
    <ProtectedRoute pageId="employee_dashboard">
      <EmployeeDashboard />
    </ProtectedRoute>
  } />
</Routes>
```

### Pattern 3: Lazy Load Pages Based on Role

```javascript
const pageComponents = {
  'employee_dashboard': () => import('./pages/EmployeeDashboard'),
  'hr_dashboard': () => import('./pages/HRDashboard'),
  'enterprise_dashboard': () => import('./pages/EnterpriseDashboard'),
  'system_admin': () => import('./pages/SystemAdmin'),
};

async function loadAvailablePages(userRole) {
  const response = await fetch('/api/pages/accessible');
  const pages = response.json().data;
  
  const availablePages = {};
  
  for (const page of pages) {
    if (pageComponents[page.pageId]) {
      availablePages[page.pageId] = await pageComponents[page.pageId]();
    }
  }
  
  return availablePages;
}
```

---

## Caching Strategy

```javascript
// Cache accessible pages to reduce API calls
const pageCache = new Map();
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

async function getAccessiblePagesCached(token) {
  if (pageCache.has(token)) {
    const cached = pageCache.get(token);
    if (Date.now() - cached.timestamp < CACHE_DURATION) {
      return cached.pages;
    }
  }
  
  const response = await fetch('/api/pages/accessible', {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  
  const pages = response.json().data;
  pageCache.set(token, { pages, timestamp: Date.now() });
  
  return pages;
}

// Clear cache on logout
function logout() {
  pageCache.delete(currentToken);
  currentToken = null;
}
```

---

## Error Handling

```javascript
async function checkPageAccessSafe(pageId) {
  try {
    const token = localStorage.getItem('token');
    
    if (!token) {
      // User not logged in
      redirectToLogin();
      return false;
    }
    
    const response = await fetch(
      `http://localhost:8081/api/pages/check/${pageId}`,
      {
        headers: { 'Authorization': 'Bearer ' + token }
      }
    );
    
    if (response.status === 401) {
      // Token expired
      refreshToken();
      return await checkPageAccessSafe(pageId); // Retry
    }
    
    if (response.status === 403) {
      // Access denied
      return false;
    }
    
    const data = await response.json();
    return data.data;
    
  } catch (error) {
    console.error('Error checking page access:', error);
    // Fail safely - deny access
    return false;
  }
}
```

---

## Deployment Considerations

### Environment Configuration

```javascript
// config.js
export const USER_SERVICE_URL = process.env.REACT_APP_USER_SERVICE_URL 
  || 'http://localhost:8081';

// Usage
const response = await fetch(
  `${USER_SERVICE_URL}/api/pages/accessible`,
  { headers }
);
```

### Production Setup

```bash
# .env.production
REACT_APP_USER_SERVICE_URL=https://user-service.yourcompany.com
```

---

## Rate Limiting Recommendation

User Service doesn't implement rate limiting yet. Consider:

1. **Frontend**: Cache page access results (as shown above)
2. **Backend**: Add rate limiting middleware
3. **API Gateway**: Implement rate limiting at gateway level

---

## Next Steps for Web Team

1. ✅ Get accessible pages on login
2. ✅ Build dynamic navigation menu
3. ✅ Add page access checks before navigation
4. ✅ Show role-specific content
5. ✅ Hide restricted UI elements
6. ✅ Implement permission guards for routes
7. ✅ Cache pages to reduce API calls
8. ✅ Handle token refresh scenarios

---

## Support & Debugging

### Check if page access API is working

```bash
# Login first
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@company.com",
    "password": "pass",
    "enterpriseId": "ent-001"
  }'

# Get token from response
TOKEN="eyJhbGc..."

# Check accessible pages
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/pages/accessible

# Check specific page
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/pages/check/employee_dashboard
```

---

**Your microservice is ready for integration!** 🚀

