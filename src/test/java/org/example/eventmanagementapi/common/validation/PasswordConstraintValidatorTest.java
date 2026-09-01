package org.example.eventmanagementapi.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConstraintValidatorTest {

    private ValidPassword.PasswordConstraintValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidPassword.PasswordConstraintValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "StrongPass1!",
            "Admin@2026",
            "Complex#Password9"
    })
    @DisplayName("Should return true for valid complex passwords")
    void shouldAcceptValidPasswords(final String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",       // Less than 8 chars
            "nouppercase1!", // Missing uppercase
            "NOLOWERCASE1!", // Missing lowercase
            "NoDigits!@#$",  // Missing digit
            "NoSpecialChar1" // Missing special char
    })
    @DisplayName("Should return false for passwords not meeting strength criteria")
    void shouldRejectWeakPasswords(final String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }
}
