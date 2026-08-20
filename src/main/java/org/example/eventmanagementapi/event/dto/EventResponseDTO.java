package org.example.eventmanagementapi.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String title,
        BigDecimal basePrice,
        Integer soldTicketsCount,
        LocalDateTime dateAndTime,
        UUID venueId,
        String venueName,
        String cityName,
        List<String> performerNames
) {
}
