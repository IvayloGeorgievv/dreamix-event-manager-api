package org.example.eventmanagementapi.auth.dto;

import org.example.eventmanagementapi.user.Role;
import java.util.UUID;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        UUID id,
        String email,
        String firstName,
        String lastName,
        Role role
) {
}