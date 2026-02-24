package com.pm.notificationservice.templates.impl;

import com.pm.notificationservice.templates.EmailTemplate;
import java.util.Map;

/**
 * OTP Email Template
 */
public class OtpEmailTemplate implements EmailTemplate {

    @Override
    public String getSubject() {
        return "Your OTP Code";
    }

    @Override
    public String buildContent(Map<String, Object> data) {
        String otp = (String) data.getOrDefault("otp", "XXXXXX");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                        .header { text-align: center; color: #333; }
                        .otp-box { background-color: #f0f0f0; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0; }
                        .otp-code { font-size: 32px; font-weight: bold; color: #2c3e50; letter-spacing: 5px; }
                        .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; }
                        .warning { color: #e74c3c; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>One-Time Password (OTP) Verification</h2>
                        </div>
                        <p>Hello,</p>
                        <p>Your OTP code is:</p>
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                        </div>
                        <p><span class="warning">⚠️ This code expires in 5 minutes</span></p>
                        <p>Do not share this code with anyone. If you did not request this code, please ignore this email.</p>
                        <div class="footer">
                            <p>This is an automated message, please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, otp);
    }

    @Override
    public String buildPlainText(Map<String, Object> data) {
        String otp = (String) data.getOrDefault("otp", "XXXXXX");
        return String.format(
                "Your OTP Code\n\n" +
                "Your OTP is: %s\n\n" +
                "This code expires in 5 minutes. Do not share it with anyone.\n\n" +
                "If you did not request this code, please ignore this email.",
                otp);
    }
}

