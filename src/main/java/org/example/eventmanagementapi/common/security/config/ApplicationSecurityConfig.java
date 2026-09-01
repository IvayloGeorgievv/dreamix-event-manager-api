package org.example.eventmanagementapi.common.security.config;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    private final UserRepository userRepository;

    // Defines how Spring Security fetches the user entity and authorities from the database by email
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmailAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    // Configures the authentication strategy by linking the user lookup service with the password hashing tool
    @Bean
    public AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Exposes Spring's central AuthenticationManager bean to process authentication attempts during login
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration configuration) {
        try {
            return configuration.getAuthenticationManager();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to configure AuthenticationManager", ex);
        }
    }

    // Defines BCrypt as the standard password hashing algorithm for storing and verifying credentials
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
