package org.example.eventmanagementapi.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceRedisTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenServiceRedisImpl refreshTokenService;

    private static final String EMAIL = "john@example.com";
    private static final String TOKEN = "refresh-token-123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should store refresh token with TTL in Redis")
    void shouldStoreRefreshToken() {
        refreshTokenService.storeRefreshToken(EMAIL, TOKEN);

        verify(valueOperations).set(
                eq("refresh_token:" + EMAIL),
                eq(TOKEN),
                eq(Duration.ofMillis(604800000L))
        );
    }

    @Test
    @DisplayName("Should validate matching refresh token")
    void shouldValidateStoredRefreshToken() {
        when(valueOperations.get("refresh_token:" + EMAIL)).thenReturn(TOKEN);

        boolean isValid = refreshTokenService.isRefreshTokenValid(EMAIL, TOKEN);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should increment user token version and revoke refresh token")
    void shouldIncrementVersionAndRevokeToken() {
        refreshTokenService.incrementUserTokenVersion(EMAIL);

        verify(valueOperations).increment("user_token_version:" + EMAIL);
        verify(redisTemplate).delete("refresh_token:" + EMAIL);
    }
}