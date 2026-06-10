package com.example.arbor.dto.response;

public record DashboardPublicoResponseDTO(
        Long totalArvores,
        Long arvoresSaudaveis,
        Long arvoresAcompanhamento
) {}
