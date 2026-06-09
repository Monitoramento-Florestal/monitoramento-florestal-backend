package com.example.arbor.dto.response;

import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.enums.TipoSolicitacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record ListItemAprovacaoResponseDTO(
        UUID id,
        TipoSolicitacao tipo,
        StatusRegistro status,
        UUID pesquisadorId,
        LocalDateTime dataSubmissao
) {}
