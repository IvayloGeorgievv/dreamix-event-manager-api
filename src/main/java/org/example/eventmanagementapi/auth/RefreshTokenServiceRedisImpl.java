package org.example.eventmanagementapi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceRedisImpl implements RefreshTokenRedisService {

    private static final String REDIS_PREFIX = "refresh_token:";
    private static final String VERSION_PREFIX = "user_token_version:";

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void storeRefreshToken(String email, String refreshToken) {
        String key = REDIS_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(refreshExpiration));
    }

    @Override
    public boolean isRefreshTokenValid(String email, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(REDIS_PREFIX + email);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    @Override
    public void revokeRefreshToken(String email) {
        redisTemplate.delete(REDIS_PREFIX + email);
    }

    @Override
    public int getOrInitializeUserTokenVersion(String email) {
        String key = VERSION_PREFIX + email;
        String currentVersion = redisTemplate.opsForValue().get(key);

        if(currentVersion == null) {
            redisTemplate.opsForValue().set(key, "1");
            return 1;
        }
        return Integer.parseInt(currentVersion);
    }

    @Override
    public boolean isTokenVersionValid(String email, int tokenVersion) {
        String key = VERSION_PREFIX + email;
        String currentVersion = redisTemplate.opsForValue().get(key);

        if(currentVersion == null) {
            redisTemplate.opsForValue().set(key, "1");
            return tokenVersion == 1;
        }
        return Integer.parseInt(currentVersion) == tokenVersion;
    }

    @Override
    public void incrementUserTokenVersion(String email) {
        String key = VERSION_PREFIX + email;
        redisTemplate.opsForValue().increment(key);

        revokeRefreshToken(email);
    }
}
