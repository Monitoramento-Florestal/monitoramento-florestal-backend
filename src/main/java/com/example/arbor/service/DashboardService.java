package com.example.arbor.service;

import com.example.arbor.dto.response.DashboardAdministrativoResponseDTO;
import com.example.arbor.dto.response.DashboardPesquisadorResponseDTO;
import com.example.arbor.dto.response.DashboardPublicoResponseDTO;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.RegistroArvoreRepository;
import com.example.arbor.repository.SolicitacaoAprovacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ArvoreRepository arvoreRepository;
    private final RegistroArvoreRepository registroRepository;
    private final SolicitacaoAprovacaoRepository solicitacaoRepository;

    public DashboardPublicoResponseDTO dashboardPublico() {

        Long totalArvores = arvoreRepository.count();
        Long arvoresAcompanhamento = arvoreRepository.countArvoresInjuriadas();
        Long arvoresSaudaveis = totalArvores - arvoresAcompanhamento;

        return new DashboardPublicoResponseDTO(
                totalArvores,
                arvoresSaudaveis,
                arvoresAcompanhamento
        );
    }

    public DashboardPesquisadorResponseDTO dashboardPesquisador(UUID pesquisadorId) {

        Long totalArvores = arvoreRepository.count();
        Long arvoresInjuriadas = arvoreRepository.countArvoresInjuriadas();
        Long arvoresSaudaveis = totalArvores - arvoresInjuriadas;
        Long solicitacoesPendentes = solicitacaoRepository.countByPesquisadorIdAndStatus(
                pesquisadorId, StatusRegistro.PENDENTE);
        Long registrosCriados = registroRepository.countByPesquisadorId(pesquisadorId);

        return new DashboardPesquisadorResponseDTO(
                arvoresSaudaveis,
                solicitacoesPendentes,
                registrosCriados,
                totalArvores
        );
    }

    public DashboardAdministrativoResponseDTO dashboardAdministrativo(UUID id) {

        Long totalArvores = arvoreRepository.count();
        Long arvoresInjuriadas = arvoreRepository.countArvoresInjuriadas();
        Long arvoresSaudaveis = totalArvores - arvoresInjuriadas;
        Long arvoresCortadas = arvoreRepository.countByAtivaFalse();
        Long aprovacoesPendentes = solicitacaoRepository.countByStatus(StatusRegistro.PENDENTE);

        return new DashboardAdministrativoResponseDTO(
                totalArvores,
                arvoresSaudaveis,
                arvoresInjuriadas,
                arvoresCortadas,
                aprovacoesPendentes
        );
    }

}
