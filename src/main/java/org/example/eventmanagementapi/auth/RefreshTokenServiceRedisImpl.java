package org.example.eventmanagementapi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceRedisImpl implements RefreshTokenService {

    @Value("${spring.data.redis.refresh-token.prefix}")
    private String redisPrefix;

    @Value("${spring.data.redis.refresh-token.version-prefix}")
    private String versionPrefix;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void storeRefreshToken(final String email, final String refreshToken) {
        final String key = redisPrefix + email;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(refreshExpiration));
    }

    @Override
    public boolean isRefreshTokenValid(final String email, final String refreshToken) {
        final String storedToken = redisTemplate.opsForValue().get(redisPrefix + email);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    @Override
    public void revokeRefreshToken(final String email) {
        redisTemplate.delete(redisPrefix + email);
    }

    @Override
    public int getOrInitializeUserTokenVersion(final String email) {
        final String key = versionPrefix + email;
        final String currentVersion = redisTemplate.opsForValue().get(key);

        if(currentVersion == null) {
            redisTemplate.opsForValue().set(key, "1");
            return 1;
        }
        return Integer.parseInt(currentVersion);
    }

    @Override
    public boolean isTokenVersionValid(final String email, final int tokenVersion) {
        final String key = versionPrefix + email;
        final String currentVersion = redisTemplate.opsForValue().get(key);

        if(currentVersion == null) {
            redisTemplate.opsForValue().set(key, "1");
            return tokenVersion == 1;
        }
        return Integer.parseInt(currentVersion) == tokenVersion;
    }

    @Override
    public void incrementUserTokenVersion(final String email) {
        final String key = versionPrefix + email;
        redisTemplate.opsForValue().increment(key);

        revokeRefreshToken(email);
    }
}
