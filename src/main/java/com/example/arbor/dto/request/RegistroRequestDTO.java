package com.example.arbor.dto.request;

import com.example.arbor.model.CondicaoArvore;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroRequestDTO(
        @NotNull LocalDateTime dataColeta,
        @NotNull UUID arvoreId,
        @NotNull Double altura,
        @NotNull CondicaoArvore condicao
) {}
