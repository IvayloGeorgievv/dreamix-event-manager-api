package org.example.eventmanagementapi.dto.ticket;

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
