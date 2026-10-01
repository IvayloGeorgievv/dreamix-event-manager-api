package org.example.eventmanagementapi.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BatchTicketPurchaseRequestDTO(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotEmpty(message = "At least one seat must be selected")
        @Size(max = 6, message = "Cannot purchase more than 6 tickets at once")
        List<@NotBlank @Size(max = 50) String> seatNumbers,

        @Size(max = 30)
        String phoneNumber,

        @Size(max = 200)
        String address,

        @Size(max = 20)
        String postalCode
) {
}
