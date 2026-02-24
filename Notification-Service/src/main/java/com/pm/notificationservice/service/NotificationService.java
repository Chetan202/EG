package com.pm.notificationservice.service;

import com.pm.notificationservice.dto.NotificationRequest;
import com.pm.notificationservice.dto.NotificationResponse;
import com.pm.notificationservice.enums.NotificationChannel;
import com.pm.notificationservice.enums.NotificationEvent;
import com.pm.notificationservice.service.channel.EmailSender;
import com.pm.notificationservice.service.channel.SmsSender;
import com.pm.notificationservice.templates.EmailTemplate;
import com.pm.notificationservice.templates.EmailTemplateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Production-level Notification Service
 *
 * - Handles multiple notification channels (email, SMS)
 * - Supports different event types with templates
 * - Asynchronous processing
 * - Error handling and resilience
 * - Decoupled from caller - Auth Service doesn't care how notifications are sent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final EmailTemplateFactory templateFactory;

    /**
     * Send notification to recipients based on request
     * This is the main entry point called by Auth Service
     *
     * @param request notification request with event, channels, recipients, and data
     * @return notification response with status
     */
    public NotificationResponse sendNotification(NotificationRequest request) {
        String requestId = UUID.randomUUID().toString();

        // Validate request
        if (request == null || request.getChannels() == null || request.getChannels().isEmpty()) {
            log.warn("Invalid notification request: {}", requestId);
            return NotificationResponse.error(requestId, "No notification channels specified");
        }

        if (request.getTo() == null || request.getTo().isEmpty()) {
            log.warn("No recipients specified for notification: {}", requestId);
            return NotificationResponse.error(requestId, "No recipients specified");
        }

        log.info("Processing notification request: {} | Event: {} | Channels: {} | Recipients: {}",
                requestId, request.getEvent(), request.getChannels(), request.getTo().size());

        // Send notification asynchronously
        sendAsync(requestId, request);

        // Return immediate response (fire and forget pattern)
        return NotificationResponse.success(requestId, request.getEvent(), request.getTo().size());
    }

    /**
     * Async notification sending - processes in background
     * This decouples the notification sending from the caller
     */
    @Async
    protected void sendAsync(String requestId, NotificationRequest request) {
        List<String> failedChannels = new ArrayList<>();

        // Process each channel
        for (String channel : request.getChannels()) {
            try {
                NotificationChannel notifChannel = NotificationChannel.fromValue(channel);
                boolean success = switch (notifChannel) {
                    case EMAIL -> sendEmailNotification(requestId, request);
                    case SMS -> sendSmsNotification(requestId, request);
                };

                if (!success) {
                    failedChannels.add(channel);
                }

            } catch (IllegalArgumentException e) {
                log.warn("Unknown notification channel: {}", channel);
                failedChannels.add(channel);
            } catch (Exception e) {
                log.error("Error sending notification via channel '{}': {}", channel, e.getMessage());
                failedChannels.add(channel);
            }
        }

        if (!failedChannels.isEmpty()) {
            log.warn("Notification {} - Failed channels: {}", requestId, failedChannels);
        } else {
            log.info("Notification {} sent successfully through all channels", requestId);
        }
    }

    /**
     * Send email notification
     */
    private boolean sendEmailNotification(String requestId, NotificationRequest request) {
        try {
            log.debug("Sending email notification: {}", requestId);

            String event = request.getEvent();
            List<String> recipients = request.getTo();
            Map<String, Object> data = request.getData();

            // Send using template
            boolean success = emailSender.sendWithTemplate(recipients, event, data);

            if (success) {
                log.info("Email notification sent successfully: {} | Event: {} | Recipients: {}",
                        requestId, event, recipients.size());
            } else {
                log.error("Failed to send email notification: {}", requestId);
            }

            return success;

        } catch (Exception e) {
            log.error("Exception while sending email notification: {}", requestId, e);
            return false;
        }
    }

    /**
     * Send SMS notification
     */
    private boolean sendSmsNotification(String requestId, NotificationRequest request) {
        try {
            log.debug("Sending SMS notification: {}", requestId);

            if (!smsSender.isConfigured()) {
                log.warn("SMS service not configured. Skipping SMS for request: {}", requestId);
                return true; // Don't fail if SMS is not configured
            }

            List<String> recipients = request.getTo();
            String message = buildSmsMessage(request.getEvent(), request.getData());

            boolean success = smsSender.send(recipients, message);

            if (success) {
                log.info("SMS notification sent successfully: {} | Recipients: {}",
                        requestId, recipients.size());
            } else {
                log.error("Failed to send SMS notification: {}", requestId);
            }

            return success;

        } catch (Exception e) {
            log.error("Exception while sending SMS notification: {}", requestId, e);
            return false;
        }
    }

    /**
     * Build SMS message based on event and data
     */
    private String buildSmsMessage(String eventType, Map<String, Object> data) {
        return switch (eventType.toLowerCase()) {
            case "otp" -> String.format(
                    "Your OTP is: %s. Valid for 5 minutes. Do not share.",
                    data.getOrDefault("otp", "XXXXXX"));
            case "magic_link" -> "Click to login: " + data.getOrDefault("magicLink", "https://example.com");
            case "welcome" -> "Welcome! Your account is ready to use.";
            default -> "You have a new notification.";
        };
    }

    /**
     * Legacy method for backward compatibility (if needed)
     * Now delegates to new NotificationRequest-based method
     */
    public void send(Map<String, Object> payload) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .enterpriseId((String) payload.getOrDefault("enterpriseId", "default"))
                    .event((String) payload.get("event"))
                    .channels((List<String>) payload.get("channels"))
                    .to((List<String>) payload.get("to"))
                    .data((Map<String, Object>) payload.get("data"))
                    .build();

            sendNotification(request);
        } catch (Exception e) {
            log.error("Error processing legacy notification request", e);
        }
    }
}

