package com.example.arbor.service;

import com.example.arbor.dto.request.ArvoreRequestDTO;
import com.example.arbor.dto.response.ArvoreResponseDTO;
import com.example.arbor.model.Arvore;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import com.example.arbor.repository.ArvoreRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArvoreService {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private final ArvoreRepository repository;

    public ArvoreService(ArvoreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> listarTodas() {
        return repository.findByAtivaTrue().stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArvoreResponseDTO buscarPorId(UUID id) {
        Arvore arvore = repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RuntimeException("Arvore ativa nao encontrada com o ID: " + id));
        return new ArvoreResponseDTO(arvore);
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> buscarPorEspecie(String especie) {
        return repository.findByEspecieContainingIgnoreCaseAndAtivaTrue(especie).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> buscarPorEstadoGeral(EstadoGeral estadoGeral) {
        return repository.findByEstadoGeralAndAtivaTrue(estadoGeral).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> buscarPorVigor(Vigor vigor) {
        return repository.findByVigorAndAtivaTrue(vigor).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> buscarPorProblemaCopa(Problema problema) {
        return repository.findByProblemasCopaContainingAndAtivaTrue(problema).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> buscarPorProblemaTronco(Problema problema) {
        return repository.findByProblemasTroncoContainingAndAtivaTrue(problema).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArvoreResponseDTO> buscarPorProblemaRaiz(Problema problema) {
        return repository.findByProblemasRaizContainingAndAtivaTrue(problema).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArvoreResponseDTO salvar(ArvoreRequestDTO dto, Usuario executor) {
        if (executor.getPerfilAcesso() == Perfil.PUBLICO_GERAL) {
            throw new RuntimeException("Acesso negado: publico nao tem permissao para cadastrar arvores.");
        }

        Arvore arvore = new Arvore();
        arvore.setCodigo(repository.gerarProximoCodigo());
        atributosArvore(dto, arvore);

        return new ArvoreResponseDTO(repository.save(arvore));
    }

    @Transactional
    public void deletar(UUID id, Usuario executor) {
        if (!isOperadorAdministrativo(executor)) {
            throw new RuntimeException("Acesso negado: Apenas gestores ou administradores podem excluir registros.");
        }

        Arvore arvore = repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RuntimeException("Arvore ativa nao encontrada"));

        arvore.setAtiva(false);
        repository.save(arvore);
    }

    @Transactional
    public ArvoreResponseDTO atualizar(UUID id, ArvoreRequestDTO dto, Usuario executor) {
        if (executor.getPerfilAcesso() == Perfil.PUBLICO_GERAL) {
            throw new RuntimeException("Acesso negado: publico nao pode atualizar arvores.");
        }

        Arvore arvore = repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RuntimeException("Arvore ativa nao encontrada"));

        atributosArvore(dto, arvore);
        return new ArvoreResponseDTO(repository.save(arvore));
    }

    private void atributosArvore(ArvoreRequestDTO dto, Arvore arvore) {
        arvore.setEspecie(dto.especie());
        arvore.setNomeComum(dto.nomeComum());
        arvore.setBairro(dto.bairro());
        arvore.setRua(dto.rua());
        arvore.setReferencia(dto.referencia());
        arvore.setLocalizacao(toPoint(dto.lat(), dto.lng()));
        arvore.setAlturaAtual(dto.alturaAtual());
        arvore.setDapAtual(dto.dapAtual());
        arvore.setCopaAtual(dto.copaAtual());
        arvore.setEstadoGeral(dto.estadoGeral());
        arvore.setVigor(dto.vigor());
        arvore.setProblemasCopa(dto.problemasCopa());
        arvore.setProblemasRaiz(dto.problemasRaiz());
        arvore.setProblemasTronco(dto.problemasTronco());
        arvore.setEstruturaTronco(dto.estruturaTronco());
        arvore.setEstruturaBase(dto.estruturaBase());
        arvore.setEstruturaCopa(dto.estruturaCopa());
        arvore.setInclinacao(dto.inclinacao());
        arvore.setAncoragem(dto.ancoragem());
        arvore.setFluxoAutomovel(dto.fluxoAutomovel());
        arvore.setFluxoPedestre(dto.fluxoPedestre());
        arvore.setTipoVia(dto.tipoVia());
        arvore.setAlvosPotenciais(dto.alvosPotenciais());
        arvore.setAlvosSensiveis(dto.alvosSensiveis());
        arvore.setConflito(dto.conflito());
        arvore.setManejo(dto.manejo());
        arvore.setObservacoes(dto.observacoes());
    }

    private Point toPoint(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return null;
        }

        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    private boolean isOperadorAdministrativo(Usuario usuario) {
        return usuario != null
                && usuario.getPerfilAcesso() != null
                && usuario.getPerfilAcesso().isAdministrativo();
    }
}
