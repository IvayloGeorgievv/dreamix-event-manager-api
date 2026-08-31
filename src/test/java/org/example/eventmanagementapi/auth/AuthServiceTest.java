package org.example.eventmanagementapi.auth;

import org.example.eventmanagementapi.auth.dto.AuthResponseDTO;
import org.example.eventmanagementapi.auth.dto.LoginRequestDTO;
import org.example.eventmanagementapi.auth.dto.RefreshTokenDTO;
import org.example.eventmanagementapi.auth.dto.RegisterRequestDTO;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.security.jwt.JwtService;
import org.example.eventmanagementapi.user.User;
import org.example.eventmanagementapi.user.UserRepository;
import org.example.eventmanagementapi.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository customerRepository;

    @Mock
    private RefreshTokenRedisService refreshTokenRedisService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setEmail("john@example.com");
        customer.setPassword("encodedPassword");
        customer.setRole(Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("Should successfully register customer and issue tokens")
    void shouldRegisterCustomer() {
        RegisterRequestDTO request = new RegisterRequestDTO("John", "Doe", "john@example.com", "Password123!");

        when(customerRepository.existsByEmailAndDeletedFalse(request.email())).thenReturn(false);
        when(authMapper.toCustomer(request)).thenReturn(customer);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(refreshTokenRedisService.getOrInitializeUserTokenVersion(request.email())).thenReturn(1);
        when(jwtService.generateToken(customer, 1)).thenReturn("jwt-access-token");
        when(jwtService.generateRefreshToken(customer, 1)).thenReturn("jwt-refresh-token");

        AuthResponseDTO response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-access-token");
        assertThat(response.refreshToken()).isEqualTo("jwt-refresh-token");
        verify(customerRepository).save(customer);
        verify(refreshTokenRedisService).storeRefreshToken(request.email(), "jwt-refresh-token");
    }

    @Test
    @DisplayName("Should login user and store new refresh token session")
    void shouldLoginUser() {
        LoginRequestDTO request = new LoginRequestDTO("john@example.com", "Password123!");

        when(customerRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(customer));
        when(refreshTokenRedisService.getOrInitializeUserTokenVersion(request.email())).thenReturn(1);
        when(jwtService.generateToken(customer, 1)).thenReturn("jwt-access-token");
        when(jwtService.generateRefreshToken(customer, 1)).thenReturn("jwt-refresh-token");

        AuthResponseDTO response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-access-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRedisService).storeRefreshToken(request.email(), "jwt-refresh-token");
    }

    @Test
    @DisplayName("Should throw BusinessLogicException when refresh token version does not match active Redis version")
    void shouldRejectRefreshTokenOnVersionMismatch() {
        RefreshTokenDTO request = new RefreshTokenDTO("stale-refresh-token");
        String email = "john@example.com";

        when(jwtService.isTokenValid(request.refreshToken())).thenReturn(true);
        when(jwtService.extractUsername(request.refreshToken())).thenReturn(email);
        when(refreshTokenRedisService.isRefreshTokenValid(email, request.refreshToken())).thenReturn(true);
        when(refreshTokenRedisService.getOrInitializeUserTokenVersion(email)).thenReturn(2); // Updated version
        when(jwtService.extractTokenVersion(request.refreshToken())).thenReturn(1); // Stale token version

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("Token version mismatch. Please log in again.");
    }

    @Test
    @DisplayName("Should invoke Redis revocation on logout")
    void shouldLogoutUser() {
        authService.logout("john@example.com");

        verify(refreshTokenRedisService).revokeRefreshToken("john@example.com");
    }
}