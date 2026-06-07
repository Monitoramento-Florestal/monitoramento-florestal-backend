package com.example.arbor.service;

import com.example.arbor.dto.request.RecusarRegistroRequestDTO;
import com.example.arbor.dto.request.RegistroNovaArvoreRequestDTO;
import com.example.arbor.dto.request.RegistroRequestDTO;
import com.example.arbor.dto.response.RegistroNovaArvoreResponseDTO;
import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.Arvore;
import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.*;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.RegistroArvoreRepository;
import com.example.arbor.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroArvoreService {

    private final RegistroArvoreRepository registroRepository;
    private final ArvoreRepository arvoreRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<RegistroResponseDTO> filtrarPorStatus(StatusRegistro status) {
        return registroRepository.findByStatus(status).stream()
                .map(RegistroResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistroResponseDTO> filtrarPorPesquisadorId(UUID id) {
        return registroRepository.findByPesquisadorId(id).stream()
                .map(RegistroResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistroResponseDTO> filtrarPorStatusEPesquisadorId(StatusRegistro status, UUID id) {
        return registroRepository.findByStatusAndPesquisadorId(status, id).stream()
                .map(RegistroResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistroResponseDTO> filtrarPorArvore(UUID id) {
        return registroRepository.findByArvoreIdOrderByDataColetaDesc(id).stream()
                .map(RegistroResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RegistroResponseDTO aprovarRegistro(UUID registroId, Usuario admin) {
        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro nao encontrado"));

        if (registro.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException("Somente registros pendentes podem ser aprovados");
        }

        Usuario adminPersistido = usuarioRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Administrador nao encontrado"));

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
                .orElseThrow(() -> new RuntimeException("Registro nao encontrado"));

        if (registro.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException("Somente registros pendentes podem ser recusados");
        }

        Usuario adminPersistido = usuarioRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Administrador nao encontrado"));

        registro.setStatus(StatusRegistro.RECUSADO);
        registro.setMotivoRecusa(dto.motivoRecusa());
        registro.setAdministradorResponsavel(adminPersistido);
        registro.setDataAnalise(LocalDateTime.now());

        return new RegistroResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public RegistroResponseDTO cadastrar(RegistroRequestDTO dto, Usuario pesquisador) {
        Usuario pesquisadorPersistido = usuarioRepository.findById(pesquisador.getId())
                .orElseThrow(() -> new RuntimeException("Pesquisador nao encontrado"));

        Arvore arvore = arvoreRepository.findByIdAndAtivaTrue(dto.arvoreId())
                .orElseThrow(() -> new RuntimeException("Arvore ativa nao encontrada"));

        RegistroArvore registro = new RegistroArvore();
        registro.setPesquisador(pesquisadorPersistido);
        registro.setDataColeta(LocalDateTime.now());
        registro.setArvore(arvore);
        atributosRegistro(
                registro,
                dto.alturaColetada(),
                dto.dapColetada(),
                dto.copaColetada(),
                dto.estadoGeral(),
                dto.vigor(),
                dto.problemasCopa(),
                dto.problemasRaiz(),
                dto.problemasTronco(),
                dto.estruturaTronco(),
                dto.estruturaBase(),
                dto.estruturaCopa(),
                dto.inclinacaoTronco(),
                dto.ancoragem(),
                dto.fluxoPedestre(),
                dto.fluxoAutomovel(),
                dto.tipoVia(),
                dto.alvosPotenciais(),
                dto.alvosSensiveis(),
                dto.conflito(),
                dto.manejo(),
                dto.observacoes()
        );

        return new RegistroResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public RegistroNovaArvoreResponseDTO cadastrarNovaArvore(RegistroNovaArvoreRequestDTO dto, Usuario pesquisador) {
        Usuario pesquisadorPersistido = usuarioRepository.findById(pesquisador.getId())
                .orElseThrow(() -> new RuntimeException("Pesquisador nao encontrado"));

        RegistroArvore registro = new RegistroArvore();
        registro.setEspecie(dto.especie());
        registro.setBairro(dto.bairro());
        registro.setRua(dto.rua());
        registro.setReferencia(dto.referencia());
        registro.setPesquisador(pesquisadorPersistido);
        registro.setDataColeta(LocalDateTime.now());
        atributosRegistro(
                registro,
                dto.alturaColetada(),
                dto.dapColetada(),
                dto.copaColetada(),
                dto.estadoGeral(),
                dto.vigor(),
                dto.problemasCopa(),
                dto.problemasRaiz(),
                dto.problemasTronco(),
                dto.estruturaTronco(),
                dto.estruturaBase(),
                dto.estruturaCopa(),
                dto.inclinacaoTronco(),
                dto.ancoragem(),
                dto.fluxoPedestre(),
                dto.fluxoAutomovel(),
                dto.tipoVia(),
                dto.alvosPotenciais(),
                dto.alvosSensiveis(),
                dto.conflito(),
                dto.manejo(),
                dto.observacoes()
        );

        return new RegistroNovaArvoreResponseDTO(registroRepository.save(registro));
    }

    @Transactional
    public void deletar(UUID registroId, Usuario executor) {
        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro nao encontrado"));

        Usuario pesquisadorResponsavel = registro.getPesquisador();

        if (!pesquisadorResponsavel.getId().equals(executor.getId())
                && !isOperadorAdministrativo(executor)) {
            throw new RuntimeException(
                    "Acesso negado: Apenas o pesquisador responsavel, gestores ou administradores podem excluir registros.");
        }

        registroRepository.delete(registro);
    }

    private void atributosArvore(RegistroArvore registro, Arvore arvore) {
        arvore.setAlturaAtual(registro.getAlturaColetada());
        arvore.setDapAtual(registro.getDapColetada());
        arvore.setCopaAtual(registro.getCopaColetada());
        arvore.setEstadoGeral(registro.getEstadoGeral());
        arvore.setVigor(registro.getVigor());
        arvore.setProblemasCopa(copySet(registro.getProblemasCopa()));
        arvore.setProblemasRaiz(copySet(registro.getProblemasRaiz()));
        arvore.setProblemasTronco(copySet(registro.getProblemasTronco()));
        arvore.setEstruturaTronco(registro.getEstruturaTronco());
        arvore.setEstruturaBase(registro.getEstruturaBase());
        arvore.setEstruturaCopa(registro.getEstruturaCopa());
        arvore.setInclinacao(registro.getInclinacao());
        arvore.setAncoragem(registro.getAncoragem());
        arvore.setFluxoAutomovel(registro.getFluxoAutomovel());
        arvore.setFluxoPedestre(registro.getFluxoPedestre());
        arvore.setTipoVia(registro.getTipoVia());
        arvore.setAlvosPotenciais(copySet(registro.getAlvosPotenciais()));
        arvore.setAlvosSensiveis(copySet(registro.getAlvosSensiveis()));
        arvore.setConflito(copyConflito(registro.getConflito()));
        arvore.setManejo(copyManejo(registro.getManejo()));
        arvore.setObservacoes(registro.getObservacoes());
    }

    private void atributosRegistro(
            RegistroArvore registro,
            Double alturaColetada,
            Double dapColetada,
            Double copaColetada,
            EstadoGeral estadoGeral,
            Vigor vigor,
            Set<Problema> problemasCopa,
            Set<Problema> problemasRaiz,
            Set<Problema> problemasTronco,
            EstruturaTronco estruturaTronco,
            EstruturaBase estruturaBase,
            EstruturaCopa estruturaCopa,
            InclinacaoTronco inclinacaoTronco,
            AncoragemRadicular ancoragem,
            FluxoPedestre fluxoPedestre,
            FluxoAutomovel fluxoAutomovel,
            TipoVia tipoVia,
            Set<AlvoPotencial> alvosPotenciais,
            Set<AlvoSensivel> alvosSensiveis,
            Conflito conflito,
            Manejo manejo,
            String observacoes
    ) {
        registro.setStatus(StatusRegistro.PENDENTE);
        registro.setAlturaColetada(alturaColetada);
        registro.setDapColetada(dapColetada);
        registro.setCopaColetada(copaColetada);
        registro.setEstadoGeral(estadoGeral);
        registro.setVigor(vigor);
        registro.setProblemasCopa(problemasCopa);
        registro.setProblemasRaiz(problemasRaiz);
        registro.setProblemasTronco(problemasTronco);
        registro.setEstruturaTronco(estruturaTronco);
        registro.setEstruturaBase(estruturaBase);
        registro.setEstruturaCopa(estruturaCopa);
        registro.setInclinacao(inclinacaoTronco);
        registro.setAncoragem(ancoragem);
        registro.setFluxoPedestre(fluxoPedestre);
        registro.setFluxoAutomovel(fluxoAutomovel);
        registro.setTipoVia(tipoVia);
        registro.setAlvosPotenciais(alvosPotenciais);
        registro.setAlvosSensiveis(alvosSensiveis);
        registro.setConflito(conflito);
        registro.setManejo(manejo);
        registro.setObservacoes(observacoes);
        registro.setAdministradorResponsavel(null);
        registro.setDataAnalise(null);
        registro.setMotivoRecusa(null);
    }

    private boolean isOperadorAdministrativo(Usuario usuario) {
        return usuario != null
                && usuario.getPerfilAcesso() != null
                && usuario.getPerfilAcesso().isAdministrativo();
    }

    private <T> Set<T> copySet(Set<T> source) {
        if (source == null) {
            return null;
        }

        return new LinkedHashSet<>(source);
    }

    private Conflito copyConflito(Conflito source) {
        if (source == null) {
            return null;
        }

        return new Conflito(
                source.getFiacao(),
                source.getCalcada(),
                source.getIluminacao(),
                source.getEdificacao()
        );
    }

    private Manejo copyManejo(Manejo source) {
        if (source == null) {
            return null;
        }

        Manejo manejo = new Manejo();
        manejo.setAcoes(copySet(source.getAcoes()));
        manejo.setPrioridade(source.getPrioridade());
        return manejo;
    }
}
