package org.example.eventmanagementapi.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VenueRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be positive")
        Integer capacity,

        @NotNull(message = "Building ID is required")
        UUID buildingId
) {
}
