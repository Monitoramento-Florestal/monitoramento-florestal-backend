package com.example.arbor.service;

import com.example.arbor.dto.request.*;
import com.example.arbor.dto.response.AcaoAprovacaoResponseDTO;
import com.example.arbor.dto.response.DetalheAprovacaoResponseDTO;
import com.example.arbor.dto.response.ListItemAprovacaoResponseDTO;
import com.example.arbor.model.*;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.enums.TipoSolicitacao;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.RegistroArvoreRepository;
import com.example.arbor.repository.SolicitacaoAprovacaoRepository;
import com.example.arbor.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitacaoAprovacaoService {

    private final SolicitacaoAprovacaoRepository solicitacaoRepository;
    private final RegistroArvoreRepository registroRepository;
    private final ArvoreRepository arvoreRepository;
    private final UsuarioRepository usuarioRepository;

    public DetalheAprovacaoResponseDTO buscarPorId(UUID id) {
        SolicitacaoAprovacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        return new DetalheAprovacaoResponseDTO(
                solicitacao.getId(),
                solicitacao.getTipo(),
                solicitacao.getStatus(),
                solicitacao.getPesquisadorId(),
                solicitacao.getRevisorId(),
                solicitacao.getDataSubmissao(),
                solicitacao.getDataRevisao(),
                solicitacao.getMotivoRecusa(),
                solicitacao.getArvoreId(),
                solicitacao.getRegistroId(),
                solicitacao.getPropostaArvore(),
                solicitacao.getPropostaRegistro()
        );
    }

    public List<ListItemAprovacaoResponseDTO> listar(
            UUID id,
            TipoSolicitacao tipo,
            StatusRegistro status,
            UUID pesquisadorId,
            LocalDateTime dataSubmissao
    ) {
        List<SolicitacaoAprovacao> solicitacoes;

        if (id != null) {
            solicitacoes = List.of(
                    solicitacaoRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException("Solicitação não encontrada"))
            );
        }

        else if (pesquisadorId != null && status != null) {
            solicitacoes = solicitacaoRepository
                    .findByPesquisadorIdAndStatus(pesquisadorId, status);
        }

        else if (tipo != null) {
            solicitacoes = solicitacaoRepository
                    .findByTipo(tipo);
        }

        else if (dataSubmissao != null) {
            solicitacoes = solicitacaoRepository
                    .findByDataSubmissao(dataSubmissao);
        }

        else if (status != null) {
            solicitacoes = solicitacaoRepository
                    .findByStatusOrderByDataSubmissaoDesc(status);
        }

        else {
            solicitacoes = solicitacaoRepository.findAll();
        }

        return solicitacoes.stream()
                .map(s -> new ListItemAprovacaoResponseDTO(
                        s.getId(),
                        s.getTipo(),
                        s.getStatus(),
                        s.getPesquisadorId(),
                        s.getDataSubmissao()
                ))
                .toList();
    }

    @Transactional
    public UUID criarSolicitacaoCriacaoArvore(AprovacaoCriacaoArvoreRequestDTO dto, Usuario pesquisador) {

        SolicitacaoAprovacao solicitacao = new SolicitacaoAprovacao();

        solicitacao.setTipo(TipoSolicitacao.CRIACAO_ARVORE);
        solicitacao.setStatus(StatusRegistro.PENDENTE);

        solicitacao.setPesquisadorId(pesquisador.getId());
        solicitacao.setDataSubmissao(LocalDateTime.now());

        solicitacao.setPropostaArvore(dto.propostaArvore());
        solicitacao.setPropostaRegistro(dto.propostaRegistro());

        return solicitacaoRepository.save(solicitacao).getId();
    }

    @Transactional
    public UUID criarSolicitacaoCriacaoRegistro(AprovacaoCriacaoRegistroRequestDTO dto, Usuario pesquisador) {

        SolicitacaoAprovacao solicitacao = new SolicitacaoAprovacao();

        solicitacao.setTipo(TipoSolicitacao.CRIACAO_REGISTRO);
        solicitacao.setStatus(StatusRegistro.PENDENTE);

        solicitacao.setPesquisadorId(pesquisador.getId());
        solicitacao.setDataSubmissao(LocalDateTime.now());

        arvoreRepository.findById(dto.arvoreId())
                .orElseThrow(() -> new RuntimeException("Árvore não encontrada"));

        solicitacao.setArvoreId(dto.arvoreId());
        solicitacao.setPropostaRegistro(dto.propostaRegistro());

        return solicitacaoRepository.save(solicitacao).getId();
    }

    @Transactional
    public UUID criarSolicitacaoEdicaoRegistro(AprovacaoEdicaoRegistroRequestDTO dto, Usuario pesquisador) {

        SolicitacaoAprovacao solicitacao = new SolicitacaoAprovacao();

        solicitacao.setTipo(TipoSolicitacao.EDICAO_REGISTRO);
        solicitacao.setStatus(StatusRegistro.PENDENTE);

        solicitacao.setPesquisadorId(pesquisador.getId());
        solicitacao.setDataSubmissao(LocalDateTime.now());

        registroRepository.findById(dto.registroId())
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        solicitacao.setRegistroId(dto.registroId());
        solicitacao.setPropostaRegistro(dto.propostaRegistro());

        return solicitacaoRepository.save(solicitacao).getId();
    }

    @Transactional
    public AcaoAprovacaoResponseDTO aprovar(UUID solicitacaoId, Usuario gestor) {

        SolicitacaoAprovacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException(
                    "Somente solicitações pendentes podem ser aprovadas");
        }

        switch (solicitacao.getTipo()) {
            case CRIACAO_ARVORE -> aprovarCriacaoArvore(solicitacao);
            case CRIACAO_REGISTRO -> aprovarCriacaoRegistro(solicitacao);
            case EDICAO_REGISTRO -> aprovarEdicaoRegistro(solicitacao);
        }

        solicitacao.setStatus(StatusRegistro.APROVADO);
        solicitacao.setRevisorId(gestor.getId());
        solicitacao.setDataRevisao(LocalDateTime.now());

        solicitacaoRepository.save(solicitacao);

        return new AcaoAprovacaoResponseDTO(
                solicitacao.getId(), solicitacao.getStatus(), "Solicitação aprovada com sucesso"
        );
    }

    @Transactional
    public AcaoAprovacaoResponseDTO recusar(UUID solicitacaoId, Usuario gestor, RecusarAprovacaoRequestDTO dto) {

        SolicitacaoAprovacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException(
                    "Somente solicitações pendentes podem ser recusadas");
        }

        if (dto.motivoRecusa() == null || dto.motivoRecusa().isBlank()) {
            throw new RuntimeException("Motivo da recusa é obrigatório");
        }

        solicitacao.setStatus(StatusRegistro.RECUSADO);
        solicitacao.setMotivoRecusa(dto.motivoRecusa());
        solicitacao.setRevisorId(gestor.getId());
        solicitacao.setDataRevisao(LocalDateTime.now());

        solicitacaoRepository.save(solicitacao);

        return new AcaoAprovacaoResponseDTO(
                solicitacao.getId(), solicitacao.getStatus(), "Solicitação recusada com sucesso"
        );
    }

    private void aprovarCriacaoArvore(SolicitacaoAprovacao solicitacao) {
        Arvore arvore = new Arvore();
        arvore.setEspecie(solicitacao.getPropostaArvore().especie());
        arvore.setBairro(solicitacao.getPropostaArvore().bairro());
        arvore.setRua(solicitacao.getPropostaArvore().rua());
        arvore.setReferencia(solicitacao.getPropostaArvore().referencia());
        aplicarPropostaEmArvore(arvore, solicitacao.getPropostaRegistro());
        arvoreRepository.save(arvore);

        registroRepository.save(buildRegistro(solicitacao, arvore, null));
    }

    private void aprovarCriacaoRegistro(SolicitacaoAprovacao solicitacao) {
        Arvore arvore = arvoreRepository.findById(solicitacao.getArvoreId())
                .orElseThrow(() -> new RuntimeException("Árvore não encontrada"));

        aplicarPropostaEmArvore(arvore, solicitacao.getPropostaRegistro());
        arvoreRepository.save(arvore);

        registroRepository.save(buildRegistro(solicitacao, arvore, null));
    }

    private void aprovarEdicaoRegistro(SolicitacaoAprovacao solicitacao) {
        RegistroArvore registroOrigem = registroRepository.findById(solicitacao.getRegistroId())
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        Arvore arvore = registroOrigem.getArvore();
        aplicarPropostaEmArvore(arvore, solicitacao.getPropostaRegistro());
        arvoreRepository.save(arvore);

        registroRepository.save(buildRegistro(solicitacao, arvore, registroOrigem.getId()));
    }

    private void aplicarPropostaEmArvore(Arvore arvore, PropostaRegistro proposta) {
        arvore.setAlturaAtual(proposta.alturaColetada());
        arvore.setDapAtual(proposta.dapColetada());
        arvore.setCopaAtual(proposta.copaColetada());
        arvore.setEstadoGeral(proposta.estadoGeral());
        arvore.setVigor(proposta.vigor());
        arvore.setProblemasCopa(proposta.problemasCopa());
        arvore.setProblemasTronco(proposta.problemasTronco());
        arvore.setProblemasRaiz(proposta.problemasRaiz());
        arvore.setEstruturaTronco(proposta.estruturaTronco());
        arvore.setEstruturaBase(proposta.estruturaBase());
        arvore.setEstruturaCopa(proposta.estruturaCopa());
        arvore.setInclinacao(proposta.inclinacao());
        arvore.setAncoragem(proposta.ancoragem());
        arvore.setFluxoPedestre(proposta.fluxoPedestre());
        arvore.setFluxoAutomovel(proposta.fluxoAutomovel());
        arvore.setTipoVia(proposta.tipoVia());
        arvore.setAlvosPotenciais(proposta.alvosPotenciais());
        arvore.setAlvosSensiveis(proposta.alvosSensiveis());
        arvore.setConflito(proposta.conflito());
        arvore.setManejo(proposta.manejo());
        arvore.setObservacoes(proposta.observacoes());
    }

    private RegistroArvore buildRegistro(SolicitacaoAprovacao solicitacao, Arvore arvore, UUID registroOrigemId) {
        PropostaRegistro proposta = solicitacao.getPropostaRegistro();

        RegistroArvore registro = new RegistroArvore();
        registro.setEspecie(arvore.getEspecie());
        registro.setBairro(arvore.getBairro());
        registro.setRua(arvore.getRua());
        registro.setReferencia(arvore.getReferencia());
        registro.setArvore(arvore);
        registro.setDataColeta(solicitacao.getDataSubmissao());
        registro.setRegistroOrigemId(registroOrigemId);
        registro.setPesquisador(usuarioRepository.getReferenceById(solicitacao.getPesquisadorId()));
        registro.setAlturaColetada(proposta.alturaColetada());
        registro.setDapColetada(proposta.dapColetada());
        registro.setCopaColetada(proposta.copaColetada());
        registro.setEstadoGeral(proposta.estadoGeral());
        registro.setVigor(proposta.vigor());
        registro.setProblemasCopa(proposta.problemasCopa());
        registro.setProblemasTronco(proposta.problemasTronco());
        registro.setProblemasRaiz(proposta.problemasRaiz());
        registro.setEstruturaTronco(proposta.estruturaTronco());
        registro.setEstruturaBase(proposta.estruturaBase());
        registro.setEstruturaCopa(proposta.estruturaCopa());
        registro.setInclinacao(proposta.inclinacao());
        registro.setAncoragem(proposta.ancoragem());
        registro.setFluxoPedestre(proposta.fluxoPedestre());
        registro.setFluxoAutomovel(proposta.fluxoAutomovel());
        registro.setTipoVia(proposta.tipoVia());
        registro.setAlvosPotenciais(proposta.alvosPotenciais());
        registro.setAlvosSensiveis(proposta.alvosSensiveis());
        registro.setConflito(proposta.conflito());
        registro.setManejo(proposta.manejo());
        registro.setObservacoes(proposta.observacoes());
        return registro;
    }
}