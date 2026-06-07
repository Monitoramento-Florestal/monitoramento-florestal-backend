package com.example.arbor.exception;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends ApiException {

    public RequisicaoInvalidaException(String mensagem) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_REQUEST", mensagem);
    }
}
