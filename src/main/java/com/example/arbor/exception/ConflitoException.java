package com.example.arbor.exception;

import org.springframework.http.HttpStatus;

public class ConflitoException extends ApiException {

    public ConflitoException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }
}
