package com.example.arbor.controller;

import com.example.arbor.dto.*;
import com.example.arbor.model.StatusRegistro;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.RegistroArvoreService;
import com.example.arbor.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/registros")
@CrossOrigin(origins = "*")
public class RegistroArvoreController {

    private final RegistroArvoreService registroService;
    private final UsuarioService usuarioService;

    public RegistroArvoreController(RegistroArvoreService registroService, UsuarioService usuarioService) {
        this.registroService = registroService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorStatus(@PathVariable StatusRegistro status) {
        return ResponseEntity.ok(registroService.filtrarPorStatus(status));
    }

    @GetMapping("/pesquisador/{id}")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorPesquisadorId(@PathVariable UUID id) {
        return ResponseEntity.ok(registroService.filtrarPorPesquisadorId(id));
    }

    @GetMapping("/status/{status}/pesquisador/{id}")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorStatusAndPesquisadorId(@PathVariable StatusRegistro status,
                                                                                    @PathVariable UUID id) {
        return ResponseEntity.ok(registroService.filtrarPorStatusEPesquisadorId(status, id));
    }

    @GetMapping("/arvore/{id}")
    public ResponseEntity<List<RegistroResponseDTO>> filtrarPorArvoreId(@PathVariable UUID id) {
        return ResponseEntity.ok(registroService.filtrarPorArvore(id));
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<RegistroResponseDTO> aprovar(@PathVariable UUID id,
                                       @RequestBody AprovarRegistroRequestDTO dto){
        return ResponseEntity.ok(registroService.aprovarRegistro(id, dto));
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<RegistroResponseDTO> recusar(@PathVariable UUID id,
                                       @RequestBody RecusarRegistroRequestDTO dto){
        return ResponseEntity.ok(registroService.recusarRegistro(id, dto));
    }

    @PostMapping
    public ResponseEntity<RegistroResponseDTO> cadastrar(@RequestBody RegistroRequestDTO dto){
        return ResponseEntity.ok(registroService.cadastrar(dto));
    }

    @PostMapping("/nova-arvore")
    public ResponseEntity<RegistroNovaArvoreResponseDTO> cadastrarNovaArvore(
            @RequestBody RegistroNovaArvoreRequestDTO dto){
        return ResponseEntity.ok(registroService.cadastrarNovaArvore(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RegistroResponseDTO> deletar(@PathVariable UUID id,
                                                       @RequestHeader("executor-id") UUID executorId){
        Usuario executor = usuarioService.buscarEntidadePorId(executorId);
        registroService.deletar(id, executor);
        return ResponseEntity.noContent().build();
    }
}
