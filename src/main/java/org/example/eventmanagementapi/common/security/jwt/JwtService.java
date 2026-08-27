package org.example.eventmanagementapi.common.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.function.Function;

public interface JwtService {

    String generateToken(UserDetails userDetails, int tokenVersion);

    String generateRefreshToken(UserDetails userDetails, int tokenVersion);

    String extractUsername(String token);

    List<String> extractRoles(String token);

    Integer extractTokenVersion(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    boolean isTokenValid(String token);

    boolean isTokenExpired(String token);
}
