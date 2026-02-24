package com.pm.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for page access information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageAccessDto {
    private String pageId;
    private String displayName;
    private Set<String> allowedRoles;
}

