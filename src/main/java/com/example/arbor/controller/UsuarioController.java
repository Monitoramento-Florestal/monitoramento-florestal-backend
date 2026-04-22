package com.example.arbor.controller;

import com.example.arbor.dto.UsuarioRequestDTO;
import com.example.arbor.dto.UsuarioResponseDTO;
import com.example.arbor.model.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/email")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @GetMapping("/perfil/{perfil}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorPerfil(@PathVariable Perfil perfil) {
        return ResponseEntity.ok(service.buscarPorPerfil(perfil));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
            @RequestBody UsuarioRequestDTO dto,
            @RequestHeader(value = "executor-id", required = false) UUID executorId) {

        Usuario executor = (executorId != null) ? service.buscarEntidadePorId(executorId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto, executor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @RequestHeader("executor-id") UUID executorId) {

        Usuario executor = service.buscarEntidadePorId(executorId);
        service.deletar(id, executor);
        return ResponseEntity.noContent().build();
    }
}
