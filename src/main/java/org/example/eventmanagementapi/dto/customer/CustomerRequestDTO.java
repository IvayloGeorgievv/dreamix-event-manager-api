package org.example.eventmanagementapi.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequestDTO(
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

        @Size(max = 30)
        String phoneNumber,

        @Size(max = 200)
        String addressLine,

        @Size(max = 20)
        String postalCode
) {}
