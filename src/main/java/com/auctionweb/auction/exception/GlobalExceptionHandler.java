package com.auctionweb.auction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Single place for all error handling across all controllers.
 *
 * Before: every controller had duplicated try/catch returning raw JSON strings.
 * After:  controllers throw exceptions freely; this class catches and formats them.
 *
 * Error shapes returned:
 *   Validation failure: { "error": "Validation failed", "fields": { "title": "must not be blank" } }
 *   Business logic:     { "error": "Auction not found" }
 *   Unexpected:         { "error": "An unexpected error occurred" }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Triggered when @Valid fails on a @RequestBody.
     * Returns every field that failed with its message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles all intentional business logic errors thrown from services/controllers.
     * E.g. "Auction not found", "You cannot bid on your own auction", etc.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /**
     * Catch-all for unexpected errors (bugs, DB issues, etc.)
     * Returns 500 without exposing internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}
