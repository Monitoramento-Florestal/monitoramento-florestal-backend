package com.example.arbor.service;

import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.repository.RegistroArvoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroArvoreService {

    private final RegistroArvoreRepository registroRepository;

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

    @Transactional(readOnly = true)
    public RegistroResponseDTO buscarRegistroVigenteDTO(UUID arvoreId) {
        RegistroArvore registro = buscarRegistroVigente(arvoreId);
        return registro == null ? null : new RegistroResponseDTO(registro);
    }

    @Transactional(readOnly = true)
    public RegistroArvore buscarRegistroVigente(UUID arvoreId) {
        return registroRepository
                .findTopByArvoreIdAndStatusOrderByVersaoDesc(
                        arvoreId,
                        StatusRegistro.APROVADO
                )
                .orElse(null);
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

    private boolean isOperadorAdministrativo(Usuario usuario) {
        return usuario != null
                && usuario.getPerfilAcesso() != null
                && usuario.getPerfilAcesso().isAdministrativo();
    }
}
