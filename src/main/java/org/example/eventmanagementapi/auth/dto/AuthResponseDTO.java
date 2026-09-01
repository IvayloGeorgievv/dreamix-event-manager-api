package org.example.eventmanagementapi.auth.dto;

import org.example.eventmanagementapi.auth.TokenType;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        TokenType tokenType
) {
    public AuthResponseDTO(final String accessToken, final String refreshToken) {
        this(accessToken, refreshToken, TokenType.BEARER);
    }
}
