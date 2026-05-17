package com.example.arbor.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        this::getValidationMessage,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));

        ValidationErrorResponse response = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleRequisicaoInvalida(RequisicaoInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "erro", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    private String getValidationMessage(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage();
        return defaultMessage != null ? defaultMessage : "Valor inválido";
    }

    public record ValidationErrorResponse(
            Instant timestamp,
            int status,
            String message,
            Map<String, String> errors) {
    }
}
