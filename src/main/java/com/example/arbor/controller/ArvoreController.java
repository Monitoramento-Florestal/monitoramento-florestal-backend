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

    //filtro que busca os status dos registros no sistema
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ArvoreResponseDTO>> filtrarPorStatus(@PathVariable StatusRegistro status) {
        return ResponseEntity.ok(arvoreService.filtrarPorStatus(status));
    }

    //filtro que busca as solicitações de um pesquisador(uso do pesquisador)
    @GetMapping("/pesquisador/{id}")
    public ResponseEntity<List<ArvoreResponseDTO>> filtrarPorPesquisadorId(@PathVariable UUID id) {
        return ResponseEntity.ok(arvoreService.filtrarPorPesquisadorId(id));
    }

    //filtro que busca as solicitações de um pesquisador com determinado status(uso do pesquisador)
    @GetMapping("/status/{status}/pesquisador/{id}")
    public ResponseEntity<List<ArvoreResponseDTO>> filtrarPorStatusAndPesquisadorId(@PathVariable StatusRegistro status,
                                                                                    @PathVariable UUID id) {
        return ResponseEntity.ok(arvoreService.filtrarPorStatusEPesquisadorId(status, id));
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
}
