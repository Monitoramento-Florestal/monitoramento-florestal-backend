package com.example.arbor.dto;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.CondicaoArvore;
import com.example.arbor.model.Usuario;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroRequestDTO(
        @NotNull UUID pesquisadorId,
        @NotNull LocalDateTime dataColeta,
        @NotNull UUID arvoreId,
        @NotNull Double altura,
        @NotNull CondicaoArvore condicao
) {}
