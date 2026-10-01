package org.example.eventmanagementapi.ticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserTicketResponseDTO(
        UUID ticketId,
        UUID eventId,
        String eventTitle,
        LocalDateTime eventDateAndTime,
        String venueName,
        String cityName,
        String seatNumber,
        BigDecimal pricePaid
) {}
