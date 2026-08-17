package org.example.eventmanagementapi.dto.venue;

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
