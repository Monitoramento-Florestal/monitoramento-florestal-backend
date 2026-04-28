package com.example.arbor.service;

import com.example.arbor.dto.ArvoreRequestDTO;
import com.example.arbor.dto.ArvoreResponseDTO;
import com.example.arbor.model.*;
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
        return repository.findAll().stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ArvoreResponseDTO buscarPorId(UUID id) {
        Arvore arvore = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Árvore não encontrada com o ID: " + id));
        return new ArvoreResponseDTO(arvore);
    }

    public List<ArvoreResponseDTO> filtrarPorCondicao(CondicaoArvore condicao) {
        return repository.findByCondicao(condicao).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }
/*
aqui na camada de service é nwcessario esse filtro por causa do acesso dos usuários, de modo que:
administrador/gestor(vê todos, vom foco nos pendentes)
pesquisador(Vê as suas submissões, idependente do status, e as aprovadas do sistema)
public_user(somente as aprovadas)*/
    public List<ArvoreResponseDTO> filtrarPorStatus(StatusRegistro status) {
        return repository.findByStatus(status).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }
    //pesquisador(Vê as suas submissões, idependente do status, e as aprovadas do sistema)
    public List<ArvoreResponseDTO> filtrarPorPesquisadorId(UUID id) {
        return repository.findByPesquisadorId(id).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }
    //pesquisador(Vê as suas submissões, idependente do status, e as aprovadas do sistema)
    public List<ArvoreResponseDTO> filtrarPorStatusEPesquisadorId(StatusRegistro status, UUID id) {
        return repository.findByStatusAndPesquisadorId(status, id).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    //Alem disso teria um metodo pra armazenar os registros para futuras consultas

    public List<ArvoreResponseDTO> buscarPorEspecie(String especie) {
        return repository.findByEspecieContainingIgnoreCase(especie).stream()
                .map(ArvoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArvoreResponseDTO salvar(ArvoreRequestDTO dto, Usuario executor) {
        if (executor.getPerfilAcesso() == Perfil.PUBLICO_GERAL) {
            throw new RuntimeException("Acesso negado: Público não tem permissão para cadastrar árvores.");
        }

        Arvore arvore = new Arvore();
        arvore.setEspecie(dto.especie());
        arvore.setAltura(dto.altura());
        arvore.setCondicao(dto.condicao());

        Point ponto = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));
        arvore.setLocalizacao(ponto);

        return new ArvoreResponseDTO(repository.save(arvore));
    }

    @Transactional
    public void deletar(UUID id, Usuario executor) {
        if (executor.getPerfilAcesso() != Perfil.GESTOR) {
            throw new RuntimeException("Acesso negado: Apenas gestores podem excluir registros.");
        }

        Arvore arvore = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Árvore não encontrada"));
        repository.delete(arvore);
    }
}