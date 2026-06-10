package com.example.arbor.dto.response;

public record DashboardPesquisadorResponseDTO(
        Long arvoresSaudaveis,
        Long solicitacoesPendentes,
        Long registrosCriados,
        Long totalArvores
) {}
