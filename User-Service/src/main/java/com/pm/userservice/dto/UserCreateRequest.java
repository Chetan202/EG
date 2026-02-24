package com.pm.userservice.dto;

import com.pm.userservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * User Registration/Creation DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Password is required")
    private String password;

    private String department;
    private String designation;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Invalid phone number")
    private String phoneNumber;

    private UserRole role = UserRole.EMPLOYEE;

    private String managerId; // For hierarchical structure

    @NotBlank(message = "Enterprise ID is required")
    private String enterpriseId;
}

