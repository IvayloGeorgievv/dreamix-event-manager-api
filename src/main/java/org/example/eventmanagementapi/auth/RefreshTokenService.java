package org.example.eventmanagementapi.auth;

public interface RefreshTokenService {

    void storeRefreshToken(String email, String refreshToken);

    boolean isRefreshTokenValid(String email, String refreshToken);

    void revokeRefreshToken(String email);

    int getOrInitializeUserTokenVersion(String email);

    boolean isTokenVersionValid(String email, int tokenVersion);

    void incrementUserTokenVersion(String email);
}
