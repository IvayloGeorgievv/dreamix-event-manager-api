package org.example.eventmanagementapi.venue.dto;

import java.util.UUID;

public record VenueResponseDTO(
        UUID id,
        String name,
        Integer capacity,
        UUID buildingId,
        String buildingName,
        String city,
        String address
) {
}
