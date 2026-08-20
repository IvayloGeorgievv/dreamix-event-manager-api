package org.example.eventmanagementapi.ticket.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketResponseDTO(
        UUID id,
        UUID customerId,
        String customerName,
        UUID eventId,
        String eventTitle,
        String seatNumber,
        BigDecimal pricePaid
) {
}
