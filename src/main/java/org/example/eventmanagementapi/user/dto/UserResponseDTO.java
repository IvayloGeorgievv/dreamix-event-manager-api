package org.example.eventmanagementapi.user.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String addressLine,
        String postalCode
) {}
