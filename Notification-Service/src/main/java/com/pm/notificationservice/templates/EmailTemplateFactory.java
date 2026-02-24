package com.pm.notificationservice.templates;

import com.pm.notificationservice.enums.NotificationEvent;
import com.pm.notificationservice.templates.impl.MagicLinkEmailTemplate;
import com.pm.notificationservice.templates.impl.OtpEmailTemplate;
import com.pm.notificationservice.templates.impl.WelcomeEmailTemplate;
import org.springframework.stereotype.Component;

/**
 * Factory for creating email templates based on event type
 */
@Component
public class EmailTemplateFactory {

    public EmailTemplate getTemplate(String eventType) {
        try {
            NotificationEvent event = NotificationEvent.fromValue(eventType);
            return switch (event) {
                case OTP -> new OtpEmailTemplate();
                case MAGIC_LINK -> new MagicLinkEmailTemplate();
                case WELCOME -> new WelcomeEmailTemplate();
                default -> new OtpEmailTemplate(); // fallback
            };
        } catch (IllegalArgumentException e) {
            // Return a default template for unknown events
            return new OtpEmailTemplate();
        }
    }
}

