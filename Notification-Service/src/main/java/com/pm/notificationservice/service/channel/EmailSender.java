package com.pm.notificationservice.service.channel;

import java.util.List;
import java.util.Map;

/**
 * Interface for email sending implementation
 */
public interface EmailSender {

    /**
     * Send email to recipients
     *
     * @param recipients list of email addresses
     * @param subject email subject
     * @param htmlContent HTML content
     * @param plainText plain text fallback
     * @return true if email was sent successfully
     */
    boolean send(List<String> recipients, String subject, String htmlContent, String plainText);

    /**
     * Send email with template
     *
     * @param recipients list of email addresses
     * @param templateName template name/type
     * @param data template variables
     * @return true if email was sent successfully
     */
    boolean sendWithTemplate(List<String> recipients, String templateName, Map<String, Object> data);
}

