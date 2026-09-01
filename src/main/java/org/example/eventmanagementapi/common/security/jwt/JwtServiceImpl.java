package org.example.eventmanagementapi.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String VERSION_CLAIM = "token_version";

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Value("${application.security.jwt.issuer:event-management-api}")
    private String issuer;

    @Value("${application.security.jwt.audience:event-management-clients}")
    private String audience;

    @Override
    public String generateToken(final UserDetails userDetails, final int tokenVersion) {
        return buildToken(userDetails, tokenVersion, jwtExpiration);
    }

    @Override
    public String generateRefreshToken(final UserDetails userDetails, final int tokenVersion) {
        return buildToken(userDetails, tokenVersion, refreshExpiration);
    }

    private String buildToken(final UserDetails userDetails, final int tokenVersion, final long expirationMillis) {
        final Instant now = Instant.now();
        final List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(userDetails.getUsername())
                .claim(ROLES_CLAIM, roles)
                .claim(VERSION_CLAIM, tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMillis, ChronoUnit.MILLIS)))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Retrieves the subject claim (the user's email) stored inside the token payload
    @Override
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public List<String> extractRoles(final String token) {
        return extractClaim(token, claims -> {
            final List<?> roles = claims.get(ROLES_CLAIM, List.class);
            if (roles == null) {
                return Collections.emptyList();
            }
            return roles.stream()
                    .map(Object::toString)
                    .toList();
        });
    }

    @Override
    public Integer extractTokenVersion(final String token) {
        return extractClaim(token, claims -> claims.get(VERSION_CLAIM, Integer.class));
    }

    // Generic claim extractor that parses the token claims
    @Override
    public <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Validates that the token username matches the user and that the token has not expired
    @Override
    public boolean isTokenValid(final String token) {
        try {
            return !isTokenExpired(token);
        } catch (final Exception e) {
            return false;
        }
    }

    // Checks whether the token's expiration timestamp is before the current system time
    @Override
    public boolean isTokenExpired(final String token) {
        try {
            return extractClaim(token, Claims::getExpiration).toInstant().isBefore(Instant.now());
        } catch (final ExpiredJwtException ex) {
            return true;
        }
    }

    // Verifies the signature with the secret key, decodes the JWT, and returns the claims payload
    private Claims extractAllClaims(final String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    // Decodes the Base64 secret key into an HMAC-SHA256 SecretKey object
    private SecretKey getSignInKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
