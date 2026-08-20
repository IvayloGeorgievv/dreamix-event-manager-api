package org.example.eventmanagementapi.ticket.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerTicketResponseDTO(
        UUID ticketId,
        UUID eventId,
        String eventTitle,
        String seatNumber,
        BigDecimal pricePaid
) {}
