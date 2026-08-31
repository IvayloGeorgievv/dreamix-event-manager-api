package org.example.eventmanagementapi.auth;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.auth.dto.AuthResponseDTO;
import org.example.eventmanagementapi.auth.dto.LoginRequestDTO;
import org.example.eventmanagementapi.auth.dto.RefreshTokenDTO;
import org.example.eventmanagementapi.auth.dto.RegisterRequestDTO;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.security.jwt.JwtService;
import org.example.eventmanagementapi.user.User;
import org.example.eventmanagementapi.user.UserRepository;
import org.example.eventmanagementapi.user.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository customerRepository;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (customerRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new BusinessLogicException("Customer with this email already exists!");
        }

        User customer = authMapper.toCustomer(request);
        customer.setPassword(Objects.requireNonNull(passwordEncoder.encode(request.password())));
        customer.setRole(Role.ROLE_CUSTOMER);
        customerRepository.save(customer);

        int tokenVersion = refreshTokenRedisService.getOrInitializeUserTokenVersion(customer.getEmail());

        String jwtToken = jwtService.generateToken(customer, tokenVersion);
        String refreshToken = jwtService.generateRefreshToken(customer, tokenVersion);

        refreshTokenRedisService.storeRefreshToken(customer.getEmail(), refreshToken);

        return new AuthResponseDTO(jwtToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User customer = customerRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new BusinessLogicException("Invalid email or password"));

        int tokenVersion = refreshTokenRedisService.getOrInitializeUserTokenVersion(customer.getEmail());

        String jwtToken = jwtService.generateToken(customer, tokenVersion);
        // Updates the customer's stored refresh token for the new login session
        String refreshToken = jwtService.generateRefreshToken(customer, tokenVersion);

        refreshTokenRedisService.storeRefreshToken(customer.getEmail(), refreshToken);

        return new AuthResponseDTO(jwtToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenDTO request) {
        String refreshToken = request.refreshToken();

        // Validate cryptographic signature and expiration timestamp
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessLogicException("Refresh token has expired! Please log in again.");
        }

        String email = jwtService.extractUsername(refreshToken);

        if(!refreshTokenRedisService.isRefreshTokenValid(email, refreshToken)) {
            throw new BusinessLogicException("Invalid or revoked refresh token");
        }

        int currentVersion = refreshTokenRedisService.getOrInitializeUserTokenVersion(email);
        Integer tokenVersion = jwtService.extractTokenVersion(refreshToken);

        if(tokenVersion == null || tokenVersion != currentVersion) {
            throw new BusinessLogicException("Token version mismatch. Please log in again.");
        }

        List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(refreshToken).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("")
                .authorities(authorities)
                .build();

        // Issue a new access token while keeping the same refresh token until it expires
        String newAccessToken = jwtService.generateToken(principal, currentVersion);
        return new AuthResponseDTO(newAccessToken, refreshToken);
    }

    @Override
    public void logout(String email) {
        refreshTokenRedisService.revokeRefreshToken(email);
    }

    // If we introduce admin change of permissions feature ->
    // refreshTokenRedisService.incrementUserTokenVersion(targetUserEmail);
}
