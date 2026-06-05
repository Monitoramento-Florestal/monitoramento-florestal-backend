package com.example.arbor.dto.response;

import com.example.arbor.model.enums.StatusRegistro;

import java.util.UUID;

public record AcaoAprovacaoResponseDTO(
        UUID solicitacaoId,
        StatusRegistro status,
        String mensagem
) {
}
