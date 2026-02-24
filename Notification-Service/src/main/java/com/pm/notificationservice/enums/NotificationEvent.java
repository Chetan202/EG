package com.pm.notificationservice.enums;

/**
 * Notification event types supported by the service
 */
public enum NotificationEvent {
    OTP("otp", "Your OTP Code"),
    MAGIC_LINK("magic_link", "Your Login Link"),
    WELCOME("welcome", "Welcome to the Platform"),
    PASSWORD_RESET("password_reset", "Password Reset Request"),
    ACCOUNT_VERIFICATION("account_verification", "Verify Your Account");

    private final String value;
    private final String defaultSubject;

    NotificationEvent(String value, String defaultSubject) {
        this.value = value;
        this.defaultSubject = defaultSubject;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }

    public static NotificationEvent fromValue(String value) {
        for (NotificationEvent event : values()) {
            if (event.value.equalsIgnoreCase(value)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown notification event: " + value);
    }
}

