package org.example.eventmanagementapi.performer.dto;

import java.util.UUID;

public record PerformerResponseDTO(
        UUID id,
        String name
) {}