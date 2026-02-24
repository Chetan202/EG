package com.pm.notificationservice.templates.impl;

import com.pm.notificationservice.templates.EmailTemplate;
import java.util.Map;

/**
 * Magic Link Email Template
 */
public class MagicLinkEmailTemplate implements EmailTemplate {

    @Override
    public String getSubject() {
        return "Your Login Link";
    }

    @Override
    public String buildContent(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "User");
        String magicLink = (String) data.getOrDefault("magicLink", "https://example.com/login");
        String expiryMinutes = (String) data.getOrDefault("expiryMinutes", "15");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                        .header { text-align: center; color: #333; }
                        .button-container { text-align: center; margin: 30px 0; }
                        .login-button { 
                            display: inline-block;
                            background-color: #3498db; 
                            color: white; 
                            padding: 12px 30px; 
                            text-decoration: none; 
                            border-radius: 5px; 
                            font-weight: bold;
                        }
                        .login-button:hover { background-color: #2980b9; }
                        .link-text { word-break: break-all; color: #3498db; font-size: 12px; }
                        .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                        .warning { color: #e74c3c; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Secure Login Link</h2>
                        </div>
                        <p>Hello %s,</p>
                        <p>Click the button below to login to your account:</p>
                        <div class="button-container">
                            <a href="%s" class="login-button">Login Now</a>
                        </div>
                        <p>Or copy and paste this link in your browser:</p>
                        <p class="link-text">%s</p>
                        <p><span class="warning">⚠️ This link expires in %s minutes</span></p>
                        <p>If you did not request this link, please ignore this email.</p>
                        <div class="footer">
                            <p>This is an automated message, please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, userName, magicLink, magicLink, expiryMinutes);
    }

    @Override
    public String buildPlainText(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "User");
        String magicLink = (String) data.getOrDefault("magicLink", "https://example.com/login");
        String expiryMinutes = (String) data.getOrDefault("expiryMinutes", "15");

        return String.format(
                "Your Login Link\n\n" +
                "Hello %s,\n\n" +
                "Click the link below to login:\n%s\n\n" +
                "This link expires in %s minutes.\n" +
                "If you did not request this link, please ignore this email.",
                userName, magicLink, expiryMinutes);
    }
}

