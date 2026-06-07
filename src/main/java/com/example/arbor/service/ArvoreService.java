package com.example.arbor.service;

import com.example.arbor.dto.request.ArvoreRequestDTO;
import com.example.arbor.dto.response.ArvoreResponseDTO;
import com.example.arbor.model.*;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.enums.EstadoGeral;
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

    private final ArvoreRepository repository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public ArvoreService(ArvoreRepository repository) {
        this.repository = repository;
    }

    public List<ArvoreResponseDTO> listarTodas() {
        return repository.findByAtivaTrue().stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ArvoreResponseDTO buscarPorId(UUID id) {
        Arvore arvore = repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RuntimeException("Árvore ativa não encontrada com o ID: " + id));
        return new ArvoreResponseDTO(arvore);
    }

//    public List<ArvoreResponseDTO> filtrarPorCondicao(CondicaoArvore condicao) {
//        return repository.findByCondicaoAtual(condicao).stream()
//                .map(ArvoreResponseDTO::new)
//                .collect(Collectors.toList());
//    }

    public List<ArvoreResponseDTO> buscarPorEspecie(String especie) {
        return repository.findByEspecieContainingIgnoreCaseAndAtivaTrue(especie).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<Arvore> buscarPorEstadoGeral(EstadoGeral estadoGeral) {
        return repository.findByEstadoGeralAndAtivaTrue(estadoGeral);
    }

    public List<Arvore> buscarPorVigor(Vigor vigor) {
        return repository.findByVigorAndAtivaTrue(vigor);
    }

    public List<Arvore> buscarPorProblemaCopa(Problema problema) {
        return repository.findByProblemasCopaContainingAndAtivaTrue(problema);
    }

    public List<Arvore> buscarPorProblemaTronco(Problema problema) {
        return repository.findByProblemasTroncoContainingAndAtivaTrue(problema);
    }

    public List<Arvore> buscarPorProblemaRaiz(Problema problema) {
        return repository.findByProblemasRaizContainingAndAtivaTrue(problema);
    }

// Visto que o fluxo de cadastro de arvores é via RegistroArvore quando se trata de pesquisadores
// Mas nosso sistema permite o cadastro realizado por administradores, seria melhor futuramente
// Reservar esse fluxo dessa classe para eles, sendo assim nem pesquisadores nem o publico teria essa permissão
    @Transactional
    public ArvoreResponseDTO salvar(ArvoreRequestDTO dto, Usuario executor) {
        if (executor.getPerfilAcesso() == Perfil.PUBLICO_GERAL) {
            throw new RuntimeException("Acesso negado: Público não tem permissão para cadastrar árvores.");
        }

        Arvore arvore = new Arvore();
        arvore.setEspecie(dto.especie());
        arvore.setBairro(dto.bairro());
        arvore.setRua(dto.rua());
        arvore.setReferencia(dto.referencia());
        atributos_arvore(dto, arvore);

        return new ArvoreResponseDTO(repository.save(arvore));
    }

    @Transactional
    public void deletar(UUID id, Usuario executor) {
        if (!isOperadorAdministrativo(executor)) {
            throw new RuntimeException("Acesso negado: Apenas gestores ou administradores podem excluir registros.");
        }

        Arvore arvore = repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RuntimeException("Árvore ativa não encontrada"));

        arvore.setAtiva(false);
        repository.save(arvore);
    }

    @Transactional
    public ArvoreResponseDTO atualizar(UUID id, ArvoreRequestDTO dto, Usuario executor) {
        if (executor.getPerfilAcesso() == Perfil.PUBLICO_GERAL) {
            throw new RuntimeException("Acesso negado: Público não pode atualizar árvores.");
        }

        Arvore arvore = repository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new RuntimeException("Árvore ativa não encontrada"));

        atributos_arvore(dto, arvore);

        return new ArvoreResponseDTO(repository.save(arvore));
    }

    private void atributos_arvore(ArvoreRequestDTO dto, Arvore arvore) {
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

    private boolean isOperadorAdministrativo(Usuario usuario) {
        return usuario != null
                && usuario.getPerfilAcesso() != null
                && usuario.getPerfilAcesso().isAdministrativo();
    }
}
