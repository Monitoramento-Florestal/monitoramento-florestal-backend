package com.example.arbor.controller;

import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','PESQUISADOR')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
            @RequestParam(required = false) Boolean ativo,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.listarTodos(ativo, executor));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','PESQUISADOR') or #id == authentication.principal.id")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.buscarPorId(id, executor));
    }

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole('GESTOR','PESQUISADOR')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(
            @RequestParam String email,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.buscarPorEmail(email, executor));
    }

    @GetMapping("/perfil/{perfil}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorPerfil(
            @PathVariable Perfil perfil,
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(service.buscarPorPerfil(perfil, ativo));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
            @Valid @RequestBody UsuarioRequestDTO dto,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto, executor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario executor) {
        service.deletar(id, executor);
        return ResponseEntity.noContent().build();
    }
}
