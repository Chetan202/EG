package com.pm.notificationservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String enterpriseId;
    private String event;              // "otp", "magic_link", "welcome"
    private List<String> channels;    // ["email", "sms"]
    private List<String> to;          // list of email/phone
    private Map<String, Object> data; // template variables e.g. {"otp": "123456"}
}
