package com.pm.notificationservice.templates.impl;

import com.pm.notificationservice.templates.EmailTemplate;
import java.util.Map;

/**
 * Welcome Email Template
 */
public class WelcomeEmailTemplate implements EmailTemplate {

    @Override
    public String getSubject() {
        return "Welcome to Our Platform!";
    }

    @Override
    public String buildContent(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "User");
        String tempPassword = (String) data.getOrDefault("tempPassword", "XXXXXXXXX");
        String platformName = (String) data.getOrDefault("platformName", "Our Platform");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                        .header { text-align: center; color: #333; background-color: #f8f9fa; padding: 20px; border-radius: 5px; }
                        .credentials-box { background-color: #f0f0f0; padding: 15px; border-left: 4px solid #27ae60; margin: 20px 0; }
                        .credentials-box p { margin: 5px 0; }
                        .label { color: #555; font-weight: bold; }
                        .value { color: #2c3e50; font-family: monospace; }
                        .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                        .warning { color: #e67e22; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Welcome to %s!</h2>
                        </div>
                        <p>Hello %s,</p>
                        <p>Thank you for creating an account on our platform. We're excited to have you on board!</p>
                        <p>Your account has been successfully created.</p>
                        <div class="credentials-box">
                            <p><span class="label">Temporary Password:</span></p>
                            <p><span class="value">%s</span></p>
                        </div>
                        <p><span class="warning">⚠️ Please change your password on your first login for security.</span></p>
                        <p>You can now log in and start using our platform.</p>
                        <div class="footer">
                            <p>If you have any questions, please contact our support team.</p>
                            <p>This is an automated message, please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, platformName, userName, tempPassword);
    }

    @Override
    public String buildPlainText(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "User");
        String tempPassword = (String) data.getOrDefault("tempPassword", "XXXXXXXXX");
        String platformName = (String) data.getOrDefault("platformName", "Our Platform");

        return String.format(
                "Welcome to %s!\n\n" +
                "Hello %s,\n\n" +
                "Thank you for creating an account. Your account has been successfully created.\n\n" +
                "Temporary Password: %s\n\n" +
                "Please change your password on your first login for security.\n\n" +
                "If you have any questions, please contact our support team.",
                platformName, userName, tempPassword);
    }
}

