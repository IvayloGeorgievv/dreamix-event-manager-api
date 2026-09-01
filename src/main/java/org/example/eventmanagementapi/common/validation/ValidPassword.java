package org.example.eventmanagementapi.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

@Documented
@Constraint(validatedBy = ValidPassword.PasswordConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be 8-100 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

        // Regex breakdown:
        // ^                    start-of-string
        // (?=.*\\d)            a digit must occur at least once
        // (?=.*[a-z])          a lower case letter must occur at least once
        // (?=.*[A-Z])          an upper case letter must occur at least once
        // (?=.*[@#$%^&+=!._-]) a special character must occur at least once
        // (?=\\S+$)            no whitespace allowed in the entire string
        // .{8,100}             between 8 and 100 characters
        // $                    end-of-string
        private static final Pattern PATTERN = Pattern.compile(
                "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,100}$"
        );

        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if (password == null) {
                return false;
            }
            return PATTERN.matcher(password).matches();
        }
    }
}
