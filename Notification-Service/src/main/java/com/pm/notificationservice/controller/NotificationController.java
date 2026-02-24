package com.pm.notificationservice.controller;

import com.pm.notificationservice.client.UserServiceClient;
import com.pm.notificationservice.dto.ApiResponse;
import com.pm.notificationservice.dto.NotificationRequest;
import com.pm.notificationservice.dto.NotificationResponse;
import com.pm.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Controller
 *
 * Exposes REST endpoints for other services (mainly Auth Service) to request notifications
 * Acts as the API gateway for the Notification Service
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final UserServiceClient userServiceClient;

    /**
     * Send notification
     *
     * POST /api/notifications/send
     *
     * Request body:
     * {
     *   "enterpriseId": "ent-001",
     *   "event": "otp",
     *   "channels": ["email", "sms"],
     *   "to": ["user@example.com"],
     *   "data": {
     *     "otp": "847291"
     *   }
     * }
     *
     * @param request notification request
     * @return notification response with request ID and status
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {

        log.info("Received notification request for event: {} | Channels: {} | Recipients: {}",
                request.getEvent(), request.getChannels(), request.getTo().size());

        try {
            // Validate request
            if (request.getEvent() == null || request.getEvent().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(NotificationResponse.error("", "Event type is required"));
            }

            // Send notification (async)
            NotificationResponse response = notificationService.sendNotification(request);

            log.info("Notification request accepted with ID: {}", response.getRequestId());
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error processing notification request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(NotificationResponse.error("", "Error processing notification: " + e.getMessage()));
        }
    }

    /**
     * Send notification to a specific user (with user validation via FeignClient)
     *
     * POST /api/notifications/send/{userId}
     *
     * @param userId user ID to validate and send to
     * @param request notification request
     * @return notification response
     */
    @PostMapping("/send/{userId}")
    public ResponseEntity<NotificationResponse> sendNotificationToUser(
            @PathVariable String userId,
            @Valid @RequestBody NotificationRequest request) {

        log.info("Received notification request for userId: {} | Event: {}", userId, request.getEvent());

        try {
            // Validate user exists using FeignClient
            Map<String, Object> user = userServiceClient.getUserById(userId);

            if (user == null || user.isEmpty()) {
                log.warn("User not found: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(NotificationResponse.error("", "User not found: " + userId));
            }

            // Enrich request with user info
            if (request.getData() == null) {
                request.setData(new HashMap<>());
            }
            request.getData().put("userId", userId);
            request.getData().put("userName", user.get("name"));

            // Send notification
            NotificationResponse response = notificationService.sendNotification(request);

            log.info("Notification sent to user: {} | Request ID: {}", userId, response.getRequestId());
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error processing notification for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(NotificationResponse.error("", "Error: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Notification-Service"));
    }

    /**
     * Fallback endpoint for legacy requests
     */
    @PostMapping("/send/legacy")
    public ResponseEntity<ApiResponse<Void>> sendLegacy(
            @RequestBody Map<String, Object> payload) {

        try {
            notificationService.send(payload);
            return ResponseEntity.ok(ApiResponse.success("Notification sent", null));
        } catch (Exception e) {
            log.error("Error in legacy notification endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage(), null));
        }
    }
}


