package com.pm.notificationservice.enums;

/**
 * Supported notification channels
 */
public enum NotificationChannel {
    EMAIL("email"),
    SMS("sms");

    private final String value;

    NotificationChannel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationChannel fromValue(String value) {
        for (NotificationChannel channel : values()) {
            if (channel.value.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown notification channel: " + value);
    }
}

