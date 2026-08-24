package org.example.eventmanagementapi.auth.dto;

public record AuthResponseDTO(
        String accessToken,
        String tokenType
) {
    public AuthResponseDTO(String accessToken) {
        this(accessToken, "Bearer");
    }
}
