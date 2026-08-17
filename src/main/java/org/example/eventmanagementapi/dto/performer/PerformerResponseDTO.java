package org.example.eventmanagementapi.dto.performer;

import java.util.UUID;

public record PerformerResponseDTO(
        UUID id,
        String name
) {}