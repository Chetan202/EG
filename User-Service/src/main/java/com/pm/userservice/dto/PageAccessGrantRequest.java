package com.pm.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO for granting/revoking page access
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageAccessGrantRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Page ID is required")
    private String pageId;

    @NotBlank(message = "Action is required (grant or revoke)")
    private String action; // "grant" or "revoke"

    private String reason; // Optional reason for the action
}

