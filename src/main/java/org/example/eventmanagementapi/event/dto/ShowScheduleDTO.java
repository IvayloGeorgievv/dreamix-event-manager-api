package org.example.eventmanagementapi.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShowScheduleDTO(
        UUID id,
        String city,
        UUID venueId,
        String venueName,
        String venueAddress,
        LocalDateTime dateAndTime,
        BigDecimal startingPrice
) {
}
