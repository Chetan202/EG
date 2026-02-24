package com.pm.notificationservice.service.channel;

import java.util.List;

/**
 * Interface for SMS sending implementation
 * Pluggable - can be implemented with Twilio, AWS SNS, or any SMS provider
 */
public interface SmsSender {

    /**
     * Send SMS to recipients
     *
     * @param recipients list of phone numbers
     * @param message SMS message content
     * @return true if SMS was sent successfully
     */
    boolean send(List<String> recipients, String message);

    /**
     * Check if SMS sender is configured and ready
     *
     * @return true if SMS service is available
     */
    boolean isConfigured();
}

