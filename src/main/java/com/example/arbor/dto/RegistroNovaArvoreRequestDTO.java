package com.example.arbor.dto;

import com.example.arbor.model.CondicaoArvore;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroNovaArvoreRequestDTO(
        UUID pesquisadorId,
        LocalDateTime dataColeta,
        String especie,
        Double altura,
        CondicaoArvore condicao,
        Double latitude,
        Double longitude
) {}
