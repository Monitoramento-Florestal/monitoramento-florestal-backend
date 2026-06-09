package com.example.arbor.dto.request;

import com.example.arbor.model.PropostaRegistro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AprovacaoCriacaoRegistroRequestDTO(
        @NotNull
        UUID arvoreId,

        @NotNull
        @Valid
        PropostaRegistro propostaRegistro
) {}
