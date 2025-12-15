package com.example.attachfile.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxPendingApplicationsException.class)
    public ResponseEntity<Map<String, String>> handleMaxPending(MaxPendingApplicationsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "code", "MAX_PENDING_REACHED",
                        "message", ex.getMessage()
                ));
    }

    // Optional: generic handler to return JSON for other unchecked exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "code", "INTERNAL_ERROR",
                        "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected server error"
                ));
    }
}
