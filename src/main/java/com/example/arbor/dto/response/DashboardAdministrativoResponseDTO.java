package com.example.arbor.dto.response;

public record DashboardAdministrativoResponseDTO(
        Long totalArvores,
        Long arvoresSaudaveis,
        Long arvoresInjuriadas,
        Long arvoresCortadas,
        Long aprovacoesPendentes
) {}
