package org.example.eventmanagementapi.customer.dto;

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
