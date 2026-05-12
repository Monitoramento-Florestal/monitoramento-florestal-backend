package com.example.arbor.controller;

import com.example.arbor.dto.ArvoreRequestDTO;
import com.example.arbor.dto.ArvoreResponseDTO;
import com.example.arbor.model.CondicaoArvore;
import com.example.arbor.model.StatusRegistro;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.ArvoreService;
import com.example.arbor.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/arvores")
@CrossOrigin(origins = "*")
public class ArvoreController {

    private final ArvoreService arvoreService;
    private final UsuarioService usuarioService;

    public ArvoreController(ArvoreService arvoreService, UsuarioService usuarioService) {
        this.arvoreService = arvoreService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<ArvoreResponseDTO>> listarTodas() {
        return ResponseEntity.ok(arvoreService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArvoreResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(arvoreService.buscarPorId(id));
    }

    @GetMapping("/especie")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarPorEspecie(@RequestParam String nome) {
        return ResponseEntity.ok(arvoreService.buscarPorEspecie(nome));
    }

    @GetMapping("/condicao/{condicao}")
    public ResponseEntity<List<ArvoreResponseDTO>> filtrarPorCondicao(@PathVariable CondicaoArvore condicao) {
        return ResponseEntity.ok(arvoreService.filtrarPorCondicao(condicao));
    }

    @PostMapping
    public ResponseEntity<ArvoreResponseDTO> cadastrar(
            @RequestBody ArvoreRequestDTO dto,
            @RequestHeader("executor-id") UUID executorId) {

        Usuario executor = usuarioService.buscarEntidadePorId(executorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(arvoreService.salvar(dto, executor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @RequestHeader("executor-id") UUID executorId) {

        Usuario executor = usuarioService.buscarEntidadePorId(executorId);
        arvoreService.deletar(id, executor);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArvoreResponseDTO> atualizar(
            @PathVariable UUID id,
            @RequestBody ArvoreRequestDTO dto,
            @RequestHeader("executor-id") UUID executorId) {

        Usuario executor = usuarioService.buscarEntidadePorId(executorId);
        return ResponseEntity.ok(arvoreService.atualizar(id, dto, executor));
    }
}