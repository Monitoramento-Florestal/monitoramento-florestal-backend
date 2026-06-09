package com.example.arbor.dto.request;

import com.example.arbor.model.PropostaRegistro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AprovacaoEdicaoRegistroRequestDTO(
        @NotNull
        UUID registroId,

        @NotNull
        @Valid
        PropostaRegistro propostaRegistro
) {}
