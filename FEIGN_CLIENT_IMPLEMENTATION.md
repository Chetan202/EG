# FeignClient Implementation Summary

## Overview
FeignClient has been successfully applied to both the Notification-Service and User-Service in your microservices architecture. FeignClient enables declarative REST client communication between microservices.

## Changes Made

### 1. Dependencies Added

#### Notification-Service (pom.xml)
- `spring-boot-starter-web`: Web support
- `spring-cloud-starter-openfeign`: OpenFeign library for FeignClient
- Spring Cloud version: 2024.0.0

#### User-Service (pom.xml)
- Same dependencies as Notification-Service for consistency

### 2. Application Configuration

#### Notification-Service (NotificationServiceApplication.java)
```java
@SpringBootApplication
@EnableFeignClients
public class NotificationServiceApplication {
    // ...
}
```
- Added `@EnableFeignClients` annotation to enable FeignClient scanning

#### User-Service (UserServiceApplication.java)
```java
@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {
    // ...
}
```

### 3. FeignClient Interface Created

**Location:** `Notification-Service/src/main/java/com/pm/notificationservice/client/UserServiceClient.java`

```java
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") String userId);

    @GetMapping("/api/users/{id}/verify")
    Map<String, Object> verifyUser(@PathVariable("id") String userId);
}
```

**Features:**
- Declarative REST client for User Service integration
- Configurable service URL via `user-service.url` property
- Two endpoints for user interaction:
  - `getUserById()`: Retrieves user information
  - `verifyUser()`: Verifies if user exists

### 4. Controller Updated

**Location:** `Notification-Service/src/main/java/com/pm/notificationservice/controller/NotificationController.java`

The NotificationController now:
- Injects `UserServiceClient` via constructor
- Uses FeignClient to validate users before sending notifications
- Returns proper API responses using the ApiResponse DTO

```java
@PostMapping("/send/{userId}")
public ResponseEntity<ApiResponse<String>> sendNotificationToUser(
        @PathVariable String userId,
        @RequestBody Map<String, Object> payload) {
    // FeignClient call to verify user
    Map<String, Object> user = userServiceClient.getUserById(userId);
    
    if (user == null || user.isEmpty()) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not found", null));
    }
    
    payload.put("userId", userId);
    notificationService.send(payload);
    
    return ResponseEntity.ok(ApiResponse.success("Notification sent to user: " + userId, userId));
}
```

### 5. API Response DTO Created

**Location:** `Notification-Service/src/main/java/com/pm/notificationservice/dto/ApiResponse.java`

Provides a generic response wrapper:
```java
@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

### 6. Configuration Updates

#### Notification-Service (application.yaml)
```yaml
# User Service URL for FeignClient
user-service:
  url: http://localhost:8081

# FeignClient Configuration
feign:
  client:
    config:
      user-service:
        connect-timeout: 5000
        read-timeout: 5000
```

#### User-Service (application.properties)
```properties
server.port=8081

# FeignClient Configuration
feign.client.config.default.connect-timeout=5000
feign.client.config.default.read-timeout=5000

# Notification Service URL for FeignClient
notification-service.url=http://localhost:8084
```

## How to Use

### Starting the Services

1. **User Service** (Port 8081):
   ```bash
   cd User-Service
   mvn spring-boot:run
   ```

2. **Notification Service** (Port 8084):
   ```bash
   cd Notification-Service
   mvn spring-boot:run
   ```

### API Endpoints

#### Send Notification to User
```bash
POST /api/notifications/send/{userId}
Content-Type: application/json

{
  "event": "otp",
  "channels": ["email"],
  "to": ["user@example.com"],
  "data": {
    "otp": "123456"
  }
}
```

The FeignClient will automatically verify the user exists in User-Service before sending the notification.

## Benefits of FeignClient

1. **Declarative**: Simple interface-based client definition
2. **Load Balancing**: Works seamlessly with Spring Cloud LoadBalancer
3. **Service Discovery**: Integrates with Eureka for dynamic service discovery
4. **Resilience**: Easy integration with circuit breakers (Hystrix/Resilience4j)
5. **Error Handling**: Centralized error handling and retry logic
6. **Interceptors**: Support for request/response interceptors
7. **Timeout Management**: Built-in timeout configuration

## Next Steps (Optional Enhancements)

1. Add error handling with `@FeignClient` error decoders
2. Implement circuit breaker pattern with Resilience4j
3. Add service discovery with Eureka Client
4. Implement custom interceptors for authentication
5. Add request/response logging
6. Configure retry logic for fault tolerance

