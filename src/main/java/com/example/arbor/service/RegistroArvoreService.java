package com.example.arbor.service;

import com.example.arbor.dto.request.RecusarRegistroRequestDTO;
import com.example.arbor.dto.request.RegistroNovaArvoreRequestDTO;
import com.example.arbor.dto.request.RegistroRequestDTO;
import com.example.arbor.dto.response.RegistroNovaArvoreResponseDTO;
import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.*;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.RegistroArvoreRepository;
import com.example.arbor.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroArvoreService {

    private final RegistroArvoreRepository registroRepository;
    private final ArvoreRepository arvoreRepository;
    private final UsuarioRepository usuarioRepository;

    public List<RegistroResponseDTO> filtrarPorStatus(StatusRegistro status){
        return registroRepository.findByStatus(status).stream()
                .map(RegistroResponseDTO::new).collect(Collectors.toList());
    }
//
    public List<RegistroResponseDTO> filtrarPorPesquisadorId(UUID id){
        return registroRepository.findByPesquisadorId(id).stream()
                .map(RegistroResponseDTO::new).collect(Collectors.toList());
    }
    //COLOQUEI OS MARCOS COMENTADOS PRA EVIDENCIAR O TRECHO DE CODIGO, A DUVIDA É:
    //DO MODO Q EU IMPLEMENTEI ISSO PEGA O USUÁRIO(PESQUISADOR) LOGADO?
    public List<RegistroResponseDTO> filtrarPorStatusEPesquisadorId(StatusRegistro status, UUID id) {
        return registroRepository.findByStatusAndPesquisadorId(status, id).stream()
                .map(RegistroResponseDTO::new).collect(Collectors.toList());
    }
//
    public List<RegistroResponseDTO> filtrarPorArvore(UUID id){
        return registroRepository.findByArvoreId(id).stream()
                .map(RegistroResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public RegistroResponseDTO aprovarRegistro(UUID registroId, Usuario admin) {

        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        if (registro.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException("Somente registros pendentes podem ser aprovados");
        }

        Usuario adminPersistido = usuarioRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        registro.setStatus(StatusRegistro.APROVADO);
        registro.setAdministradorResponsavel(adminPersistido);
        registro.setDataAnalise(LocalDateTime.now());

        if (registro.getArvore() == null) {

            Arvore arvore = new Arvore();
            arvore.setEspecie(registro.getEspecieNova());
            arvore.setAlturaAtual(registro.getAlturaColetada());
            arvore.setCondicaoAtual(registro.getCondicaoColetada());
            arvore.setLocalizacao(registro.getLocalizacaoNova());

            Arvore arvoreSalva = arvoreRepository.save(arvore);

            registro.setArvore(arvoreSalva);
        } else {
            Arvore arvoreExistente = registro.getArvore();

            arvoreExistente.setAlturaAtual(registro.getAlturaColetada());
            arvoreExistente.setCondicaoAtual(registro.getCondicaoColetada());

            arvoreRepository.save(arvoreExistente);
        }
        return new RegistroResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public RegistroResponseDTO recusarRegistro(UUID registroId, Usuario admin, RecusarRegistroRequestDTO dto) {

        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        if (registro.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException("Somente registros pendentes podem ser recusados");
        }

        Usuario adminPersistido = usuarioRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        registro.setStatus(StatusRegistro.RECUSADO);
        registro.setMotivoRecusa(dto.motivoRecusa());
        registro.setAdministradorResponsavel(adminPersistido);
        registro.setDataAnalise(LocalDateTime.now());

        return new RegistroResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public RegistroResponseDTO cadastrar(RegistroRequestDTO dto, Usuario pesquisador) {

        Usuario pesquisadorPersistido = usuarioRepository.findById(pesquisador.getId())
                .orElseThrow(() -> new RuntimeException("Pesquisador não encontrado"));

        Arvore arvore = arvoreRepository.findById(dto.arvoreId())
                .orElseThrow(() -> new RuntimeException("Árvore não encontrada"));


        RegistroArvore registro = new RegistroArvore();
        registro.setPesquisador(pesquisadorPersistido);
        registro.setDataColeta(dto.dataColeta());
        registro.setArvore(arvore);
        registro.setStatus(StatusRegistro.PENDENTE);
        registro.setAlturaColetada(dto.altura());
        registro.setCondicaoColetada(dto.condicao());

        return new RegistroResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public RegistroNovaArvoreResponseDTO cadastrarNovaArvore(RegistroNovaArvoreRequestDTO dto, Usuario pesquisador) {

        Usuario pesquisadorPersistido = usuarioRepository.findById(pesquisador.getId())
                .orElseThrow(() -> new RuntimeException("Pesquisador não encontrado"));

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        Point localizacao = geometryFactory.createPoint(
                new Coordinate(dto.longitude(), dto.latitude())
        );

        RegistroArvore registro = new RegistroArvore();
        registro.setPesquisador(pesquisadorPersistido);
        registro.setDataColeta(dto.dataColeta());
        registro.setArvore(null);
        registro.setStatus(StatusRegistro.PENDENTE);
        registro.setAlturaColetada(dto.altura());
        registro.setCondicaoColetada(dto.condicao());

        registro.setEspecieNova(dto.especie());
        registro.setLocalizacaoNova(localizacao);

        return new RegistroNovaArvoreResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public void deletar(UUID registroId, Usuario executor){

        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro não encontradO"));

        Usuario pesquisadorResponsavel = registro.getPesquisador();

        if (!pesquisadorResponsavel.getId().equals(executor.getId())
                && executor.getPerfilAcesso() != Perfil.GESTOR) {
            throw new RuntimeException("Acesso negado: Apenas o pesquisador responsável ou gestores podem excluir registros.");
        }

        registroRepository.delete(registro);
    }
}
