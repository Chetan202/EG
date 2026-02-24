package com.pm.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for page access record response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageAccessDto {

    private String id;
    private String userId;
    private String userName;
    private String pageId;
    private String pageName;
    private Boolean granted;
    private String grantedBy;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}

