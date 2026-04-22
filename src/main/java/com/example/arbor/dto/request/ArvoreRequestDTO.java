package com.example.arbor.dto.request;

import com.example.arbor.model.CondicaoArvore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArvoreRequestDTO(
        @NotBlank String especie,
        @NotNull Double altura,
        @NotNull CondicaoArvore condicao,
        @NotNull Double latitude,
        @NotNull Double longitude
) {}