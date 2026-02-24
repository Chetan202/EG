package com.pm.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for notification send operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String requestId;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private List<String> failedChannels; // channels that failed
    private String event;
    private int recipientCount;

    public static NotificationResponse success(String requestId, String event, int recipientCount) {
        return NotificationResponse.builder()
                .requestId(requestId)
                .success(true)
                .message("Notification sent successfully")
                .timestamp(LocalDateTime.now())
                .event(event)
                .recipientCount(recipientCount)
                .build();
    }

    public static NotificationResponse error(String requestId, String message) {
        return NotificationResponse.builder()
                .requestId(requestId)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

