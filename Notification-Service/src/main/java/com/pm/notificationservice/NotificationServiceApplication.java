package com.pm.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(scanBasePackages = {
        "com.example.enterprise.notification",
        "com.example.enterprise.common",
        "com.pm.notificationservice"
})
@EnableFeignClients
@EnableAsync
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}


