package org.example.eventmanagementapi.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Regex finding " with ID: <uuid>", ": <uuid>" so they can be escaped
    // Simple, non-backtracking pattern strictly matching 36-character UUID format
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(final ResourceNotFoundException ex, final HttpServletRequest request) {

        final String sanitizedMessage = sanitizeExceptionMessage(ex.getMessage());

        final ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                sanitizedMessage,
                request.getRequestURI()
        );

        log.warn("[Trace: {}] Resource not found at URI [{}]: {}", response.correlationId(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(final NoResourceFoundException ex, final HttpServletRequest request) {
        final ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Resource or endpoint not found: " + request.getRequestURI(),
                request.getRequestURI()
        );

        log.warn("[Trace: {}] Static/endpoint not found at URI [{}]", response.correlationId(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessLogicException(final BusinessLogicException ex, final HttpServletRequest request) {
        final ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("[Trace: {}] Business rule violation at URI [{}]: {}", response.correlationId(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        final Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage()));

        final ApiErrorResponse response = ApiErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                request.getRequestURI(),
                validationErrors
        );

        log.warn("[Trace: {}] Validation failed at URI [{}] with errors: {}", response.correlationId(), request.getRequestURI(), validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(final Exception ex, final HttpServletRequest request) {
        final ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected internal error occurred. Please contact support.",
                request.getRequestURI()
        );

        log.error("[Trace: {}] Unhandled server error at URI [{}]: {}", response.correlationId(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // private helper: Used to sanitize the Exception message from the UUID and still keep it explanatory
    private String sanitizeExceptionMessage(final String message) {
        if (message == null || message.isBlank()) {
            return "The requested resource was not found";
        }
        final String sanitized = UUID_PATTERN.matcher(message)
                .replaceAll("")
                .replace("with ID:", "")
                .replace("with ID", "")
                .trim();
        return sanitized.isEmpty() ? "The requested resource was not found" : sanitized;
    }
}
