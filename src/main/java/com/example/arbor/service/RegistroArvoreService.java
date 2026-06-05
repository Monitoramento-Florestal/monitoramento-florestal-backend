package com.example.arbor.service;

import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.*;
import com.example.arbor.repository.RegistroArvoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroArvoreService {

    private final RegistroArvoreRepository registroRepository;

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
    public void deletar(UUID registroId, Usuario executor){

        RegistroArvore registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro não encontradO"));

        Usuario pesquisadorResponsavel = registro.getPesquisador();

        if (!pesquisadorResponsavel.getId().equals(executor.getId())
                && !isGestorOuAdministrador(executor)) {
            throw new RuntimeException("Acesso negado: Apenas o pesquisador responsável ou gestores podem excluir registros.");
        }

        registroRepository.delete(registro);
    }
}
