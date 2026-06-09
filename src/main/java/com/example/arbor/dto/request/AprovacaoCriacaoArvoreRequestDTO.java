package com.example.arbor.dto.request;

import com.example.arbor.model.PropostaArvore;
import com.example.arbor.model.PropostaRegistro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AprovacaoCriacaoArvoreRequestDTO(
        @NotNull
        @Valid
        PropostaArvore propostaArvore,

        @NotNull
        @Valid
        PropostaRegistro propostaRegistro
) {}
