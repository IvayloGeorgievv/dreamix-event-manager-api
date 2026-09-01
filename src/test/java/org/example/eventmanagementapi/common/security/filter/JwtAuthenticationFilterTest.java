package org.example.eventmanagementapi.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eventmanagementapi.auth.RefreshTokenService;
import org.example.eventmanagementapi.common.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenRedisService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate request when JWT and token version in Redis are valid")
    void shouldAuthenticateWhenTokenAndVersionAreValid() throws ServletException, IOException {
        final String token = "valid.jwt.token";
        final String email = "john@example.com";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(jwtService.extractTokenVersion(token)).thenReturn(1);
        when(refreshTokenRedisService.isTokenVersionValid(email, 1)).thenReturn(true);
        when(jwtService.extractRoles(token)).thenReturn(List.of("ROLE_USER"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clear context and reject authentication when token version is revoked/outdated")
    void shouldNotAuthenticateWhenTokenVersionIsOutdated() throws ServletException, IOException {
        final String token = "outdated.jwt.token";
        final String email = "john@example.com";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(jwtService.extractTokenVersion(token)).thenReturn(1);
        // Version mismatch (e.g. admin changed permissions, current Redis version is 2)
        when(refreshTokenRedisService.isTokenVersionValid(email, 1)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should bypass authentication filter when Authorization header is missing")
    void shouldBypassWhenNoAuthHeader() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }
}