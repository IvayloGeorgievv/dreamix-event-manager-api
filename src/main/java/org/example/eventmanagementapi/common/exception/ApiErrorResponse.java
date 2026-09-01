package org.example.eventmanagementapi.common.exception;

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
    public static ApiErrorResponse of(final int status, final String error, final String message, final  String path) {
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
    public static ApiErrorResponse ofValidation(final int status, final String error, final String message, final String path, final Map<String, String> validationErrors) {
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