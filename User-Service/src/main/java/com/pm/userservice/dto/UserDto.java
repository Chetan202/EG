package com.pm.userservice.dto;

import com.pm.userservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String employeeId;
    private UserRole role;
    private String department;
    private String designation;
    private String phoneNumber;
    private String managerId;
    private String enterpriseId;
    private String enterpriseName;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime emailVerificationDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String profileImageUrl;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

