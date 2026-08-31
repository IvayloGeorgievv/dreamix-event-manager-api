package org.example.eventmanagementapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.eventmanagementapi.common.validation.ValidPassword;

public record RegisterRequestDTO(
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 150)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email
        @Size(max = 100)
        String email,

        @NotBlank(message = "Password is required")
        @ValidPassword
        String password
) {}
