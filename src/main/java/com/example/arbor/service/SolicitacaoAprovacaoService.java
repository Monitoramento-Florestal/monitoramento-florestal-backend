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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitacaoAprovacaoService {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private final SolicitacaoAprovacaoRepository solicitacaoRepository;
    private final RegistroArvoreRepository registroRepository;
    private final ArvoreRepository arvoreRepository;
    private final UsuarioRepository usuarioRepository;

    public List<SolicitacaoAprovacao> buscarPorStatus(ListItemAprovacaoResponseDTO dto) {
        return solicitacaoRepository.findByStatus(dto.status());
    }

    public List<SolicitacaoAprovacao> buscarPorPesquisadorEStatus(ListItemAprovacaoResponseDTO dto) {
        return solicitacaoRepository.findByPesquisadorIdAndStatus(dto.pesquisadorId(), dto.status());
    }

    public List<SolicitacaoAprovacao> buscarPorPesquisador(ListItemAprovacaoResponseDTO dto) {
        return solicitacaoRepository.findByPesquisadorId(dto.pesquisadorId());
    }

    public List<SolicitacaoAprovacao> buscarPorTipo(ListItemAprovacaoResponseDTO dto) {
        return solicitacaoRepository.findByTipo(dto.tipo());
    }

    public List<SolicitacaoAprovacao> buscarPorDataSubmissao(ListItemAprovacaoResponseDTO dto) {
        return solicitacaoRepository.findByDataSubmissao(dto.dataSubmissao());
    }

    public List<SolicitacaoAprovacao> listarPendentes() {
        return solicitacaoRepository.findByStatusOrderByDataSubmissaoDesc(StatusRegistro.PENDENTE);
    }

    public DetalheAprovacaoResponseDTO buscarPorId(UUID id, Usuario usuario) {
        SolicitacaoAprovacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
        validarAcesso(solicitacao, usuario);

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
            LocalDateTime dataSubmissao,
            Usuario usuario
    ) {
        UUID pesquisadorFiltro = resolvePesquisadorFiltro(pesquisadorId, usuario);
        List<SolicitacaoAprovacao> solicitacoes;

        if (id != null) {
            SolicitacaoAprovacao solicitacao = solicitacaoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
            validarAcesso(solicitacao, usuario);
            solicitacoes = List.of(solicitacao);
        }

        else if (pesquisadorFiltro != null && status != null) {
            solicitacoes = solicitacaoRepository
                    .findByPesquisadorIdAndStatus(pesquisadorFiltro, status);
        }

        else if (tipo != null) {
            if (!isAdministrativo(usuario)) {
                throw new RuntimeException("Pesquisador só pode consultar as próprias solicitações.");
            }
            solicitacoes = solicitacaoRepository
                    .findByTipo(tipo);
        }

        else if (dataSubmissao != null) {
            if (!isAdministrativo(usuario)) {
                throw new RuntimeException("Pesquisador só pode consultar as próprias solicitações.");
            }
            solicitacoes = solicitacaoRepository
                    .findByDataSubmissao(dataSubmissao);
        }

        else if (status != null) {
            if (pesquisadorFiltro != null) {
                solicitacoes = solicitacaoRepository
                        .findByPesquisadorIdAndStatus(pesquisadorFiltro, status);
            } else {
                solicitacoes = solicitacaoRepository
                        .findByStatusOrderByDataSubmissaoDesc(status);
            }
        }

        else {
            if (pesquisadorFiltro != null) {
                solicitacoes = solicitacaoRepository.findByPesquisadorId(pesquisadorFiltro);
            } else {
                solicitacoes = solicitacaoRepository.findAll();
            }
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

        solicitacao.setStatus(StatusRegistro.APROVADO);
        solicitacao.setRevisorId(gestor.getId());
        solicitacao.setDataRevisao(LocalDateTime.now());

        switch (solicitacao.getTipo()) {
            case CRIACAO_ARVORE -> aprovarCriacaoArvore(solicitacao);
            case CRIACAO_REGISTRO -> aprovarCriacaoRegistro(solicitacao);
            case EDICAO_REGISTRO -> aprovarEdicaoRegistro(solicitacao);
        }

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
        arvore.setCodigo(arvoreRepository.gerarProximoCodigo());
        arvore.setNomeComum(solicitacao.getPropostaArvore().nomeComum());
        arvore.setEspecie(solicitacao.getPropostaArvore().especie());
        arvore.setBairro(solicitacao.getPropostaArvore().bairro());
        arvore.setRua(solicitacao.getPropostaArvore().rua());
        arvore.setReferencia(solicitacao.getPropostaArvore().referencia());
        arvore.setLocalizacao(toPoint(
                solicitacao.getPropostaArvore().lat(),
                solicitacao.getPropostaArvore().lng()
        ));
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
        registro.setVersao(resolveVersao(arvore));
        registro.setRegistroOrigemId(registroOrigemId);
        registro.setPesquisador(usuarioRepository.getReferenceById(solicitacao.getPesquisadorId()));
        registro.setAdministradorResponsavel(
                solicitacao.getRevisorId() == null
                        ? null
                        : usuarioRepository.getReferenceById(solicitacao.getRevisorId())
        );
        registro.setDataAnalise(solicitacao.getDataRevisao());
        registro.setMotivoRecusa(null);
        registro.setStatus(StatusRegistro.APROVADO);
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

    private int resolveVersao(Arvore arvore) {
        if (arvore == null) {
            return 1;
        }

        return registroRepository.findTopByArvoreIdOrderByVersaoDesc(arvore.getId())
                .map(RegistroArvore::getVersao)
                .map(versao -> versao + 1)
                .orElse(1);
    }

    private Point toPoint(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return null;
        }

        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    private UUID resolvePesquisadorFiltro(UUID pesquisadorId, Usuario usuario) {
        if (isAdministrativo(usuario)) {
            return pesquisadorId;
        }

        if (pesquisadorId != null && !pesquisadorId.equals(usuario.getId())) {
            throw new RuntimeException("Pesquisador só pode consultar as próprias solicitações.");
        }

        return usuario.getId();
    }

    private void validarAcesso(SolicitacaoAprovacao solicitacao, Usuario usuario) {
        if (isAdministrativo(usuario)) {
            return;
        }

        if (!solicitacao.getPesquisadorId().equals(usuario.getId())) {
            throw new RuntimeException("Acesso negado: solicitação não pertence ao pesquisador autenticado.");
        }
    }

    private boolean isAdministrativo(Usuario usuario) {
        return usuario != null
                && usuario.getPerfilAcesso() != null
                && usuario.getPerfilAcesso().isAdministrativo();
    }
}
