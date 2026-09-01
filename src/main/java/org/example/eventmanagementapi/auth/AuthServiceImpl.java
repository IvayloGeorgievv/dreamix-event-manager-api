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

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenRedisService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public AuthResponseDTO register(final RegisterRequestDTO request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new BusinessLogicException("User with this email already exists!");
        }

        final User user = authMapper.toUser(request);
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(request.password())));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO login(final LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        final User user = userRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new BusinessLogicException("Invalid email or password"));

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(final RefreshTokenDTO request) {
        final String refreshToken = request.refreshToken();

        // Validate cryptographic signature and expiration timestamp
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessLogicException("Refresh token has expired! Please log in again.");
        }

        final String email = jwtService.extractUsername(refreshToken);

        if(!refreshTokenRedisService.isRefreshTokenValid(email, refreshToken)) {
            throw new BusinessLogicException("Invalid or revoked refresh token");
        }

        final int currentVersion = refreshTokenRedisService.getOrInitializeUserTokenVersion(email);
        final Integer tokenVersion = jwtService.extractTokenVersion(refreshToken);

        if(tokenVersion == null || tokenVersion != currentVersion) {
            throw new BusinessLogicException("Token version mismatch. Please log in again.");
        }

        final List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(refreshToken).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        final UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("")
                .authorities(authorities)
                .build();

        // Issue a new access token while keeping the same refresh token until it expires
        final String newAccessToken = jwtService.generateToken(principal, currentVersion);
        return new AuthResponseDTO(newAccessToken, refreshToken);
    }

    @Override
    public void logout(final String email) {
        refreshTokenRedisService.revokeRefreshToken(email);
    }

    private AuthResponseDTO generateAuthResponse(final User user) {
        final int tokenVersion = refreshTokenRedisService.getOrInitializeUserTokenVersion(user.getEmail());

        final String jwtToken = jwtService.generateToken(user, tokenVersion);
        final String refreshToken = jwtService.generateRefreshToken(user, tokenVersion);

        refreshTokenRedisService.storeRefreshToken(user.getEmail(), refreshToken);

        return new AuthResponseDTO(jwtToken, refreshToken);
    }

    // If we introduce admin change of permissions feature ->
    // refreshTokenRedisService.incrementUserTokenVersion(targetUserEmail);
}
