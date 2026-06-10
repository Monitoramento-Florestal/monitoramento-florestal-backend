package com.example.arbor.repository;

import com.example.arbor.model.SolicitacaoAprovacao;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.enums.TipoSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SolicitacaoAprovacaoRepository extends JpaRepository<SolicitacaoAprovacao, UUID> {

    List<SolicitacaoAprovacao> findByStatus(StatusRegistro status);

    Long countByStatus(StatusRegistro status);

    List<SolicitacaoAprovacao> findByPesquisadorId(UUID id);

    List<SolicitacaoAprovacao> findByPesquisadorIdAndStatus(UUID id, StatusRegistro status);

    Long countByPesquisadorIdAndStatus(UUID id, StatusRegistro status);

    List<SolicitacaoAprovacao> findByTipo(TipoSolicitacao tipo);

    List<SolicitacaoAprovacao> findByDataSubmissao(LocalDateTime data);

    List<SolicitacaoAprovacao> findByStatusOrderByDataSubmissaoDesc(StatusRegistro status);

}
