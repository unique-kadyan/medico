package com.kaddy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler with secure error messaging
 * Prevents information disclosure by not exposing stack traces or internal details
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String errorId = UUID.randomUUID().toString();

        // Log full details internally
        log.warn("Resource not found [Error ID: {}]: {}", errorId, ex.getMessage());

        // Return safe error message to client
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Resource not found",
            errorId,
            LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String errorId = UUID.randomUUID().toString();

        // Log full details internally
        log.warn("Invalid argument [Error ID: {}]: {}", errorId, ex.getMessage());

        // Return safe error message to client
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid request parameters",
            errorId,
            LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String errorId = UUID.randomUUID().toString();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed [Error ID: {}]: {} validation errors", errorId, errors.size());

        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errorId,
            LocalDateTime.now(),
            errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {

        String errorId = UUID.randomUUID().toString();

        // Log security violation
        log.warn("Access denied [Error ID: {}]: {}", errorId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Access denied",
            errorId,
            LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        String errorId = UUID.randomUUID().toString();

        // Log full details internally (including stack trace for debugging)
        log.error("Unexpected error [Error ID: {}]", errorId, ex);

        // Return GENERIC message to client (NO stack trace or internal details)
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please contact support with error ID: " + errorId,
            errorId,
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Secure error response that includes error ID for tracking
     * but doesn't expose internal implementation details
     */
    public record ErrorResponse(
        int status,
        String message,
        String errorId,
        LocalDateTime timestamp
    ) {}

    /**
     * Validation error response with field-level errors
     */
    public record ValidationErrorResponse(
        int status,
        String message,
        String errorId,
        LocalDateTime timestamp,
        Map<String, String> errors
    ) {}
}
