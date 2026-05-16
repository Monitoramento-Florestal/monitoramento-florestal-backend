package com.example.arbor.dto.request;

import com.example.arbor.model.CondicaoArvore;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RegistroNovaArvoreRequestDTO(
        @NotNull LocalDateTime dataColeta,
        @NotNull String especie,
        @NotNull Double altura,
        @NotNull CondicaoArvore condicao,
        @NotNull Double latitude,
        @NotNull Double longitude
) {}
