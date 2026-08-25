package org.example.eventmanagementapi.auth;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.auth.dto.AuthResponseDTO;
import org.example.eventmanagementapi.auth.dto.LoginRequestDTO;
import org.example.eventmanagementapi.auth.dto.RefreshTokenDTO;
import org.example.eventmanagementapi.auth.dto.RegisterRequestDTO;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.security.jwt.JwtService;
import org.example.eventmanagementapi.customer.Customer;
import org.example.eventmanagementapi.customer.CustomerRepository;
import org.example.eventmanagementapi.customer.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private final CustomerRepository customerRepository;
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

        Customer customer = authMapper.toCustomer(request);
        customer.setPassword(passwordEncoder.encode(request.password()));
        customer.setRole(Role.ROLE_CUSTOMER);

        Customer savedCustomer = customerRepository.save(customer);
        String jwtToken = jwtService.generateToken(savedCustomer);
        String refreshToken = jwtService.generateRefreshToken(savedCustomer);

        return new AuthResponseDTO(jwtToken, refreshToken);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        Customer customer = customerRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new BusinessLogicException("Invalid email or password"));

        String jwtToken = jwtService.generateToken(customer);
        // Updates the customer's stored refresh token for the new login session
        String refreshToken = jwtService.generateRefreshToken(customer);

        return new AuthResponseDTO(jwtToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenDTO request) {
        String refreshToken = request.refreshToken();

        // Locate customer by matching active stored refresh token
        Customer customer = customerRepository.findByRefreshTokenAndDeletedFalse(refreshToken)
                .orElseThrow(() -> new BusinessLogicException("Invalid or revoked refresh token!"));

        // Validate cryptographic signature and expiration timestamp
        if (!jwtService.isTokenValid(refreshToken, customer)) {
            throw new BusinessLogicException("Refresh token has expired! Please log in again.");
        }

        // Issue a new access token while keeping the same refresh token until it expires
        String newAccessToken = jwtService.generateToken(customer);
        return new AuthResponseDTO(newAccessToken, refreshToken);
    }
}
