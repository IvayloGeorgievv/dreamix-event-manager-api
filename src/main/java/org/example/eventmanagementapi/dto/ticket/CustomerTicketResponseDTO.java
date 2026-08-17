package org.example.eventmanagementapi.dto.ticket;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerTicketResponseDTO(
        UUID ticketId,
        UUID eventId,
        String eventTitle,
        String seatNumber,
        BigDecimal pricePaid
) {}
