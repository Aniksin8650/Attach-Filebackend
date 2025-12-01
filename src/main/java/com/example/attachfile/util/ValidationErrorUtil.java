package com.example.attachfile.util;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

public class ValidationErrorUtil {

    private ValidationErrorUtil() {
    }

    public static ResponseEntity<?> buildErrorResponse(BindingResult bindingResult, String defaultMessage) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError err : bindingResult.getFieldErrors()) {
            fieldErrors.put(err.getField(), err.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("message", defaultMessage);
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }
}
