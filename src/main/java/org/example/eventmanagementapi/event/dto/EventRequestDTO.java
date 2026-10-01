package org.example.eventmanagementapi.event.dto;

import jakarta.validation.constraints.*;
import org.example.eventmanagementapi.event.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 100)
        String title,

        @NotNull(message = "Category is required")
        EventCategory category,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl,

        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be positive")
        BigDecimal basePrice,

        @NotNull(message = "Date and time are required")
        @Future(message = "Event date must be in the future")
        LocalDateTime dateAndTime,

        @NotNull(message = "Venue ID is required")
        UUID venueId,

        List<@NotNull UUID> performerIds
) {
}
