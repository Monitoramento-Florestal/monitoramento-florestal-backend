package com.example.arbor.controller;

import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','PESQUISADOR') or #id == authentication.principal.id")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole('GESTOR','PESQUISADOR')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @GetMapping("/perfil/{perfil}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorPerfil(@PathVariable Perfil perfil) {
        return ResponseEntity.ok(service.buscarPorPerfil(perfil));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
            @RequestBody UsuarioRequestDTO dto,
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

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody Map<String, String> body) {
        service.solicitarResetSenha(body.get("email"));
        return ResponseEntity.ok("As instruções serão enviadas se o e-mail estiver vinculado a uma conta.");
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody Map<String, String> body) {

        service.resetarSenha(
                body.get("token"),
                body.get("novaSenha")
        );

        return ResponseEntity.ok("Senha atualizada com sucesso");
    }
}
