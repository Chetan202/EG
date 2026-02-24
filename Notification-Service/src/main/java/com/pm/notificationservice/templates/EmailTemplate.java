package com.pm.notificationservice.templates;

import java.util.Map;

/**
 * Interface for email template builders
 */
public interface EmailTemplate {

    /**
     * Get the subject line for the email
     */
    String getSubject();

    /**
     * Build the HTML content for the email
     */
    String buildContent(Map<String, Object> data);

    /**
     * Get the plain text version (fallback)
     */
    String buildPlainText(Map<String, Object> data);
}

