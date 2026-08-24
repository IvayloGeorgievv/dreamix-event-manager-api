package org.example.eventmanagementapi.auth;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.auth.dto.AuthResponseDTO;
import org.example.eventmanagementapi.auth.dto.LoginRequestDTO;
import org.example.eventmanagementapi.auth.dto.RegisterRequestDTO;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.security.jwt.JwtService;
import org.example.eventmanagementapi.customer.Customer;
import org.example.eventmanagementapi.customer.CustomerRepository;
import org.example.eventmanagementapi.customer.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

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
        return new AuthResponseDTO(jwtToken);
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
        return new AuthResponseDTO(jwtToken);
    }
}
