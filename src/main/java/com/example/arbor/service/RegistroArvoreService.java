package com.example.arbor.service;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.StatusRegistro;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.RegistroArvoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroArvoreService {
    private final RegistroArvoreRepository registroRepository;
    private final ArvoreRepository arvoreRepository;

    public RegistroArvore cadastrar(RegistroArvore registro) {
        registro.setStatus(StatusRegistro.PENDENTE);
        return registroRepository.save(registro);
    }

    public Arvore aprovarRegistro(UUID registroId, Usuario admin) {

        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        if (registro.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException("Somente registros pendentes podem ser aprovados");
        }

        registro.setStatus(StatusRegistro.APROVADO);
        registro.setAdministradorResponsavel(admin);
        registro.setDataAnalise(java.time.LocalDateTime.now());
        registroRepository.save(registro);

        Arvore arvore = new Arvore();

        arvore.setEspecie(registro.getEspecie());
        arvore.setAltura(registro.getAltura());
        arvore.setCondicao(registro.getCondicao());
        arvore.setDataRegistro(registro.getDataRegistro());
        arvore.setLocalizacao(registro.getLocalizacao());

        arvore.setDataAprovacao(java.time.LocalDate.now());

        return arvoreRepository.save(arvore);
    }

    public RegistroArvore recusarRegistro(UUID registroId, String motivo, Usuario admin) {

        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        if (registro.getStatus() != StatusRegistro.PENDENTE) {
            throw new RuntimeException("Somente registros pendentes podem ser recusados");
        }

        registro.setStatus(StatusRegistro.RECUSADO);
        registro.setMotivoRecusa(motivo);
        registro.setAdministradorResponsavel(admin);
        registro.setDataAnalise(java.time.LocalDateTime.now());

        return registroRepository.save(registro);
    }

    public List<RegistroArvore> filtrarPorStatus(StatusRegistro status){
        return registroRepository.findByStatus(status);
    }

    public List<RegistroArvore> filtrarPorPesquisador(UUID id){
        return registroRepository.findByPesquisadorId(id);
    }

    public List<RegistroArvore> filtrarPorStatusEPesquisador(StatusRegistro status, UUID id){
        return registroRepository.findByStatusAndPesquisadorId(status, id);
    }
}

