package com.example.arbor.controller;

import com.example.arbor.dto.request.AprovarRegistroRequestDTO;
import com.example.arbor.dto.request.RecusarRegistroRequestDTO;
import com.example.arbor.dto.request.RegistroNovaArvoreRequestDTO;
import com.example.arbor.dto.request.RegistroRequestDTO;
import com.example.arbor.dto.response.RegistroNovaArvoreResponseDTO;
import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.RegistroArvoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/registros")
@CrossOrigin(origins = "*")
public class RegistroArvoreController {

    private final RegistroArvoreService registroService;

    public RegistroArvoreController(RegistroArvoreService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorStatus(@PathVariable StatusRegistro status) {
        return ResponseEntity.ok(registroService.filtrarPorStatus(status));
    }

    @GetMapping("/pesquisador")
    @PreAuthorize("hasRole('PESQUISADOR')")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorPesquisador(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(registroService.filtrarPorPesquisadorId(usuario.getId()));
    }

    @GetMapping("/pesquisador/status/{status}")
    @PreAuthorize("hasRole('PESQUISADOR')")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorStatusAndPesquisador(@PathVariable StatusRegistro status,
                                                                                    @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(registroService.filtrarPorStatusEPesquisadorId(status, usuario.getId()));
    }

    @GetMapping("/arvore/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorArvoreId(@PathVariable UUID id) {
        return ResponseEntity.ok(registroService.filtrarPorArvore(id));
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<RegistroResponseDTO> aprovar(@PathVariable UUID id,
                                                       @RequestBody AprovarRegistroRequestDTO dto,
                                                       @AuthenticationPrincipal Usuario executor){
        return ResponseEntity.ok(registroService.aprovarRegistro(id, executor));
    }

    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<RegistroResponseDTO> recusar(@PathVariable UUID id,
                                                       @RequestBody RecusarRegistroRequestDTO dto,
                                                       @AuthenticationPrincipal Usuario executor){
        return ResponseEntity.ok(registroService.recusarRegistro(id, executor, dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<RegistroResponseDTO> cadastrar(@Valid @RequestBody RegistroRequestDTO dto,
                                                         @AuthenticationPrincipal Usuario executor){
        return ResponseEntity.ok(registroService.cadastrar(dto, executor));
    }

    @PostMapping("/nova-arvore")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<RegistroNovaArvoreResponseDTO> cadastrarNovaArvore(
            @Valid @RequestBody RegistroNovaArvoreRequestDTO dto,
            @AuthenticationPrincipal Usuario executor){
        return ResponseEntity.ok(registroService.cadastrarNovaArvore(dto, executor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<RegistroResponseDTO> deletar(@PathVariable UUID id,
                                                       @AuthenticationPrincipal Usuario executor){
        registroService.deletar(id, executor);
        return ResponseEntity.noContent().build();
    }

    // ← NOVO: histórico de registros por árvore
    @GetMapping("/trees/{treeId}/records")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<RegistroResponseDTO>> listarHistoricoPorArvore(
            @PathVariable UUID treeId) {
        return ResponseEntity.ok(
                registroService.listarHistoricoPorArvore(treeId)
        );
    }

    // ← NOVO: detalhe de registro validando pertencimento à árvore
    @GetMapping("/trees/{treeId}/records/{recordId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<RegistroResponseDTO> buscarRegistroDaArvore(
            @PathVariable UUID treeId,
            @PathVariable UUID recordId) {
        return ResponseEntity.ok(
                registroService.buscarRegistroDaArvore(treeId, recordId)
        );
    }
}