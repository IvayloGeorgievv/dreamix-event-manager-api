package org.example.eventmanagementapi.dto.customer;

import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String addressLine,
        String postalCode
) {}
