package com.example.arbor.exception;

import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends ApiException {

    public AcessoNegadoException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }
}
