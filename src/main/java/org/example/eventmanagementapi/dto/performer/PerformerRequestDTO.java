package org.example.eventmanagementapi.dto.performer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerformerRequestDTO(
        @NotBlank(message = "Performer name is required")
        @Size(max = 100)
        String name
) {}
