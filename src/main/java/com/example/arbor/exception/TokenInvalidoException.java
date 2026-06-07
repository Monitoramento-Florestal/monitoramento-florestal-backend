package com.example.arbor.exception;

import org.springframework.http.HttpStatus;

public class TokenInvalidoException extends ApiException {

    public TokenInvalidoException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", message);
    }
}
