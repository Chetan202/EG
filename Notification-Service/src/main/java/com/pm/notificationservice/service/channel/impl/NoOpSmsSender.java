package com.pm.notificationservice.service.channel.impl;

import com.pm.notificationservice.service.channel.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default SMS sender - logs messages without sending
 * This is a placeholder for future SMS provider integration (e.g., Twilio)
 */
@Service
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "none", matchIfMissing = true)
@Slf4j
public class NoOpSmsSender implements SmsSender {

    @Override
    public boolean send(List<String> recipients, String message) {
        log.debug("SMS not configured. Would send to {}: {}", recipients, message);
        return true; // Return true to avoid blocking notification flow
    }

    @Override
    public boolean isConfigured() {
        return false;
    }
}

