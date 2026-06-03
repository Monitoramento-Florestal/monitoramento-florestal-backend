package com.example.arbor.controller;

import com.example.arbor.dto.request.AlterarSenhaRequestDTO;
import com.example.arbor.dto.request.AtualizarPerfilRequestDTO;
import com.example.arbor.dto.response.UsuarioPerfilResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UsuarioPerfilController {

    private final UsuarioService service;

    public UsuarioPerfilController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponseDTO> buscarMeuPerfil(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.buscarMeuPerfil(usuario));
    }

    @PatchMapping("/me")
    public ResponseEntity<UsuarioPerfilResponseDTO> atualizarMeuPerfil(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AtualizarPerfilRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarMeuPerfil(usuario, dto));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> alterarMinhaSenha(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AlterarSenhaRequestDTO dto) {
        service.alterarMinhaSenha(usuario, dto);
        return ResponseEntity.noContent().build();
    }
}
