package org.example.eventmanagementapi.ticket.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketResponseDTO(
        UUID id,
        UUID userId,
        String userName,
        UUID eventId,
        String eventTitle,
        String seatNumber,
        BigDecimal pricePaid
) {
}
