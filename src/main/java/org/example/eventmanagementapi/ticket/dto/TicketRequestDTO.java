package org.example.eventmanagementapi.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TicketRequestDTO(
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotBlank(message = "Seat number is required")
        @Size(max = 50)
        String seatNumber
) {
}
