package org.example.eventmanagementapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        String correlationId,
        Map<String, String> validationErrors
) {
    // Standard error factory
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(
                status,
                error,
                message,
                path,
                Instant.now(),
                UUID.randomUUID().toString().substring(0, 8),
                null
        );
    }

    // Validation error factory
    public static ApiErrorResponse ofValidation(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ApiErrorResponse(
                status,
                error,
                message,
                path,
                Instant.now(),
                UUID.randomUUID().toString().substring(0, 8),
                validationErrors
        );
    }
}