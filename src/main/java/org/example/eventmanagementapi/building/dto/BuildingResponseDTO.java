package org.example.eventmanagementapi.building.dto;

import java.util.UUID;

public record BuildingResponseDTO(
        UUID id,
        String name,
        String city,
        String address
) {}
