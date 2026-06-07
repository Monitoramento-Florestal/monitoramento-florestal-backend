package com.example.arbor.service;

import com.example.arbor.dto.request.RecusarRegistroRequestDTO;
import com.example.arbor.dto.request.RegistroNovaArvoreRequestDTO;
import com.example.arbor.dto.request.RegistroRequestDTO;
import com.example.arbor.dto.response.RegistroNovaArvoreResponseDTO;
import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.Arvore;
import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.enums.*;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.RegistroArvoreRepository;
import com.example.arbor.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
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

    public List<RegistroResponseDTO> filtrarPorPesquisadorId(UUID id){
        return registroRepository.findByPesquisadorId(id).stream()
                .map(RegistroResponseDTO::new).collect(Collectors.toList());
    }

    public List<RegistroResponseDTO> filtrarPorStatusEPesquisadorId(StatusRegistro status, UUID id) {
        return registroRepository.findByStatusAndPesquisadorId(status, id).stream()
                .map(RegistroResponseDTO::new).collect(Collectors.toList());
    }

    public List<RegistroResponseDTO> filtrarPorArvore(UUID id) {
        return registroRepository
                .findByArvoreIdOrderByDataColetaDesc(id).stream()
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

            arvore.setEspecie(registro.getEspecie());
            arvore.setBairro(registro.getBairro());
            arvore.setRua(registro.getRua());
            arvore.setReferencia(registro.getReferencia());
            atributosArvore(registro, arvore);

            Arvore arvoreSalva = arvoreRepository.save(arvore);
            registro.setArvore(arvoreSalva);

        } else {
            Arvore arvoreExistente = registro.getArvore();

            atributosArvore(registro, arvoreExistente);

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

        Arvore arvore = arvoreRepository.findByIdAndAtivaTrue(dto.arvoreId())
                .orElseThrow(() -> new RuntimeException("Árvore ativa não encontrada"));


        RegistroArvore registro = new RegistroArvore();

        registro.setPesquisador(pesquisadorPersistido);
        registro.setDataColeta(LocalDateTime.now());
        registro.setArvore(arvore);
        atributosRegistro(registro,
                dto.alturaColetada(), dto.dapColetada(), dto.copaColetada(), dto.estadoGeral(),
                dto.vigor(), dto.problemasCopa(), dto.problemasRaiz(), dto.problemasTronco(),
                dto.estruturaTronco(), dto.estruturaBase(), dto.estruturaCopa(), dto.inclinacaoTronco(),
                dto.ancoragem(), dto.fluxoPedestre(), dto.fluxoAutomovel(), dto.tipoVia(), dto.alvosPotenciais(),
                dto.alvosSensiveis(), dto.conflito(), dto.manejo(), dto.observacoes());

        return new RegistroResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public RegistroNovaArvoreResponseDTO cadastrarNovaArvore(RegistroNovaArvoreRequestDTO dto, Usuario pesquisador) {

        Usuario pesquisadorPersistido = usuarioRepository.findById(pesquisador.getId())
                .orElseThrow(() -> new RuntimeException("Pesquisador não encontrado"));

        RegistroArvore registro = new RegistroArvore();

        registro.setEspecie(dto.especie());
        registro.setBairro(dto.bairro());
        registro.setRua(dto.rua());
        registro.setRua(dto.rua());
        registro.setPesquisador(pesquisadorPersistido);
        registro.setDataColeta(LocalDateTime.now());
        atributosRegistro(registro,
                dto.alturaColetada(), dto.dapColetada(), dto.copaColetada(), dto.estadoGeral(),
                dto.vigor(), dto.problemasCopa(), dto.problemasRaiz(), dto.problemasTronco(),
                dto.estruturaTronco(), dto.estruturaBase(), dto.estruturaCopa(), dto.inclinacaoTronco(),
                dto.ancoragem(), dto.fluxoPedestre(), dto.fluxoAutomovel(), dto.tipoVia(), dto.alvosPotenciais(),
                dto.alvosSensiveis(), dto.conflito(), dto.manejo(), dto.observacoes());

        return new RegistroNovaArvoreResponseDTO(registroRepository.save(registro));
    }

    public List<RegistroResponseDTO> listarHistoricoPorArvore(UUID arvoreId) {

        arvoreRepository.findByIdAndAtivaTrue(arvoreId)
                .orElseThrow(() ->
                        new RuntimeException("Árvore ativa não encontrada"));

        return registroRepository
                .findByArvoreIdOrderByVersaoDesc(arvoreId)
                .stream()
                .map(RegistroResponseDTO::new)
                .collect(Collectors.toList());
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

    private void atributosArvore(RegistroArvore registro, Arvore arvore) {
        arvore.setAlturaAtual(registro.getAlturaColetada());
        arvore.setDapAtual(registro.getDapColetada());
        arvore.setCopaAtual(registro.getCopaColetada());
        arvore.setEstadoGeral(registro.getEstadoGeral());
        arvore.setVigor(registro.getVigor());
        arvore.setProblemasCopa(registro.getProblemasCopa());
        arvore.setProblemasRaiz(registro.getProblemasRaiz());
        arvore.setProblemasTronco(registro.getProblemasTronco());
        arvore.setEstruturaTronco(registro.getEstruturaTronco());
        arvore.setEstruturaBase(registro.getEstruturaBase());
        arvore.setEstruturaCopa(registro.getEstruturaCopa());
        arvore.setInclinacao(registro.getInclinacao());
        arvore.setAncoragem(registro.getAncoragem());
        arvore.setFluxoAutomovel(registro.getFluxoAutomovel());
        arvore.setFluxoPedestre(registro.getFluxoPedestre());
        arvore.setTipoVia(registro.getTipoVia());
        arvore.setAlvosPotenciais(registro.getAlvosPotenciais());
        arvore.setAlvosSensiveis(registro.getAlvosSensiveis());
        arvore.setConflito(registro.getConflito());
        arvore.setManejo(registro.getManejo());
        arvore.setObservacoes(registro.getObservacoes());
    }

    private void atributosRegistro(RegistroArvore registro, Double aDouble, Double aDouble2, Double aDouble3,
                                   EstadoGeral estadoGeral, Vigor vigor, Set<Problema> problemas,
                                   Set<Problema> problemas2, Set<Problema> problemas3,
                                   EstruturaTronco estruturaTronco, EstruturaBase estruturaBase, EstruturaCopa estruturaCopa,
                                   InclinacaoTronco inclinacaoTronco, AncoragemRadicular ancoragem,
                                   FluxoPedestre fluxoPedestre, FluxoAutomovel fluxoAutomovel,
                                   TipoVia tipoVia, Set<AlvoPotencial> alvoPotencials, Set<AlvoSensivel> alvoSensivels,
                                   Conflito conflito, Manejo manejo, String observacoes)
    {
        registro.setStatus(StatusRegistro.PENDENTE);
        registro.setAlturaColetada(aDouble);
        registro.setDapColetada(aDouble2);
        registro.setCopaColetada(aDouble3);
        registro.setEstadoGeral(estadoGeral);
        registro.setVigor(vigor);
        registro.setProblemasCopa(problemas);
        registro.setProblemasRaiz(problemas2);
        registro.setProblemasTronco(problemas3);
        registro.setEstruturaTronco(estruturaTronco);
        registro.setEstruturaBase(estruturaBase);
        registro.setEstruturaCopa(estruturaCopa);
        registro.setInclinacao(inclinacaoTronco);
        registro.setAncoragem(ancoragem);
        registro.setFluxoPedestre(fluxoPedestre);
        registro.setFluxoAutomovel(fluxoAutomovel);
        registro.setTipoVia(tipoVia);
        registro.setAlvosPotenciais(alvoPotencials);
        registro.setAlvosSensiveis(alvoSensivels);
        registro.setConflito(conflito);
        registro.setManejo(manejo);
        registro.setObservacoes(observacoes);

        registro.setAdministradorResponsavel(null);
        registro.setDataAnalise(null);
        registro.setMotivoRecusa(null);
    }
}
