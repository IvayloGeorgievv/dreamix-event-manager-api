package org.example.eventmanagementapi.dto.building;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BuildingRequestDTO(
        @NotBlank(message = "Building name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Address is required")
        @Size(max = 200)
        String address
) {}
