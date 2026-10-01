package org.example.eventmanagementapi.event.dto;
import org.example.eventmanagementapi.event.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String title,
        EventCategory category,
        String description,
        String imageUrl,
        BigDecimal basePrice,
        Integer soldTicketsCount,
        LocalDateTime dateAndTime,
        UUID venueId,
        String venueName,
        String venueAddress,
        Integer venueCapacity,
        String cityName,
        List<String> performerNames,
        List<ShowScheduleDTO> schedules
) {
}
