package com.pm.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client for User Service integration.
 * Used to make HTTP calls to User Service endpoints.
 */
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {

    /**
     * Get user by ID from User Service
     *
     * @param userId the user ID
     * @return user information as Map
     */
    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") String userId);

    /**
     * Verify user exists in User Service
     *
     * @param userId the user ID
     * @return user verification status
     */
    @GetMapping("/api/users/{id}/verify")
    Map<String, Object> verifyUser(@PathVariable("id") String userId);
}

