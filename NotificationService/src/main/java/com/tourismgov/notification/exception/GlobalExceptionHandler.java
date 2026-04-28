package com.tourismgov.notification.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — Notification Service
 * ─────────────────────────────────────────────────────────────────────────────
 * Centralized error handling for all exception scenarios.
 * Every error returns a consistent JSON structure.
 *
 * Response format:
 * {
 *   "timestamp": "2026-04-28T10:00:00",
 *   "status":    404,
 *   "error":     "NOT_FOUND",
 *   "message":   "Notification #5 not found or does not belong to you."
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Application-level Exceptions (thrown explicitly in service layer)
    // ─────────────────────────────────────────────────────────────────────────

    /** Resource not found (notification, user, etc.) */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Business rule violations (e.g., access denied, service said no) */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Business Exception [{}]: {}", ex.getStatusCode(), ex.getReason());
        return build(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Request Validation Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /** @Valid / @Validated constraint failures on request body fields */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {} field error(s)", ex.getBindingResult().getErrorCount());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_FAILED");
        body.put("message", "One or more fields failed validation. Check 'fieldErrors' for details.");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /** Malformed JSON or wrong data type in request body */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST,
                "Invalid request body. Please check your JSON format and data types.");
    }

    /** Wrong type for a path variable or request parameter (e.g., letters where ID expected) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Parameter '%s' has invalid value '%s'. Expected type: %s.",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        log.warn("Type mismatch: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /** Missing required @RequestParam */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter '%s' is missing.", ex.getParameterName());
        log.warn("Missing request parameter: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /** Missing required @RequestHeader (e.g., JWT not sent, Gateway not used) */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = String.format(
                "Required header '%s' is missing. This endpoint requires a valid JWT Bearer token " +
                "sent to the API Gateway (port 8383).", ex.getHeaderName()
        );
        log.warn("Missing required header: {}", ex.getHeaderName());
        return build(HttpStatus.UNAUTHORIZED, message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. HTTP Method & Route Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /** Wrong HTTP method (e.g., GET on a POST endpoint) */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        String allowed = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream()
                        .map(Object::toString).collect(Collectors.joining(", "))
                : "N/A";
        String message = String.format(
                "HTTP method '%s' is not supported for this endpoint. Allowed: [%s].",
                ex.getMethod(), allowed
        );
        log.warn("Method not allowed: {}", message);
        return build(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Inter-Service (Feign) Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /** UserService returns 404 (user doesn't exist) */
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFound(FeignException.NotFound ex) {
        log.error("Feign 404: User not found in UserService.");
        return build(HttpStatus.NOT_FOUND, "User not found. Please verify the userId.");
    }

    /** Any other Feign error (service down, timeout, etc.) */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        log.error("Feign client error [{}]: {}", ex.status(), ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "A dependent service is currently unavailable. Please try again later.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Data Layer Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /** Unique constraint, foreign key, or other DB constraint violation */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        log.error("DB Constraint Violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT,
                "A data conflict occurred. This record may already exist.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Catch-All Fallback
    // ─────────────────────────────────────────────────────────────────────────

    /** Safety net for any unhandled exception — prevents stack traces leaking to clients */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("UNHANDLED EXCEPTION — THIS IS A BUG. Please investigate:", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal server error occurred. Our team has been notified.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                message
        );
        return ResponseEntity.status(status).body(error);
    }
}