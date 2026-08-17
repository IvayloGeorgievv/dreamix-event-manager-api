package org.example.eventmanagementapi.dto.building;

import java.util.UUID;

public record BuildingResponseDTO(
        UUID id,
        String name,
        String city,
        String address
) {}
