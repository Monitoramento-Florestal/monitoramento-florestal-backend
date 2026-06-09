package com.example.arbor.dto.response;

import com.example.arbor.model.PropostaArvore;
import com.example.arbor.model.PropostaRegistro;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.enums.TipoSolicitacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record DetalheAprovacaoResponseDTO(
        UUID id,
        TipoSolicitacao tipo,
        StatusRegistro status,

        UUID pesquisadorId,
        UUID revisorId,

        LocalDateTime dataSubmissao,
        LocalDateTime dataRevisao,

        String motivoRecusa,

        UUID arvoreId,
        UUID registroId,

        PropostaArvore propostaArvore,
        PropostaRegistro propostaRegistro
) {}
