package com.example.arbor.controller;

import com.example.arbor.dto.request.AtualizarUsuarioAdminRequestDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.PageResponseDTO;
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
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> listarTodos(
            @RequestParam(required = false) Perfil perfilAcesso,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.listarTodos(perfilAcesso, ativo, search, page, limit, executor));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR') or #id == authentication.principal.id")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.buscarPorId(id, executor));
    }

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(
            @RequestParam String email,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.buscarPorEmail(email, executor));
    }

    @GetMapping("/perfil/{perfil}")
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<java.util.List<UsuarioResponseDTO>> listarPorPerfil(
            @PathVariable Perfil perfil,
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(service.buscarPorPerfil(perfil, ativo));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
            @Valid @RequestBody UsuarioRequestDTO dto,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto, executor));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarUsuarioAdminRequestDTO dto,
            @AuthenticationPrincipal Usuario executor) {
        return ResponseEntity.ok(service.atualizarUsuario(id, dto, executor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario executor) {
        service.deletar(id, executor);
        return ResponseEntity.noContent().build();
    }
}
