package com.example.arbor.controller;

import com.example.arbor.dto.request.ArvoreRequestDTO;
import com.example.arbor.dto.response.ArvoreResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import com.example.arbor.service.ArvoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/arvores")
@CrossOrigin(origins = "*")
public class ArvoreController {

    private final ArvoreService arvoreService;

    public ArvoreController(ArvoreService arvoreService) {
        this.arvoreService = arvoreService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> listarTodas() {
        return ResponseEntity.ok(arvoreService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<ArvoreResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(arvoreService.buscarPorId(id));
    }

    @GetMapping("/especie")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarPorEspecie(@RequestParam String nome) {
        return ResponseEntity.ok(arvoreService.buscarPorEspecie(nome));
    }

    //  REFATORAR O GET POR CONDIÇÕES
/*
    @GetMapping("/condicao/{condicao}")
    @PreAuthorize("hasAnyRole('GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> filtrarPorCondicao(@PathVariable CondicaoArvore condicao) {
        return ResponseEntity.ok(arvoreService.filtrarPorCondicao(condicao));
    }
*/
    @GetMapping("/estado-geral/{estadoGeral}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<Arvore>> buscarPorEstadoGeral(
            @PathVariable EstadoGeral estadoGeral
    ) {
        return ResponseEntity.ok(
                arvoreService.buscarPorEstadoGeral(estadoGeral)
        );
    }

    @GetMapping("/vigor/{vigor}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<Arvore>> buscarPorVigor(
            @PathVariable Vigor vigor
    ) {
        return ResponseEntity.ok(
                arvoreService.buscarPorVigor(vigor)
        );
    }

    @GetMapping("/problemas/copa/{problema}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<Arvore>> buscarProblemaCopa(
            @PathVariable Problema problema
    ) {
        return ResponseEntity.ok(
                arvoreService.buscarPorProblemaCopa(problema)
        );
    }

    @GetMapping("/problemas/tronco/{problema}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<Arvore>> buscarProblemaTronco(
            @PathVariable Problema problema
    ) {
        return ResponseEntity.ok(
                arvoreService.buscarPorProblemaTronco(problema)
        );
    }

    @GetMapping("/problemas/raiz/{problema}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<Arvore>> buscarProblemaRaiz(
            @PathVariable Problema problema
    ) {
        return ResponseEntity.ok(
                arvoreService.buscarPorProblemaRaiz(problema)
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<ArvoreResponseDTO> cadastrar(
            @Valid @RequestBody ArvoreRequestDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(arvoreService.salvar(dto, usuarioLogado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        arvoreService.deletar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<ArvoreResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ArvoreRequestDTO dto,
            @AuthenticationPrincipal Usuario executor) {

        return ResponseEntity.ok(arvoreService.atualizar(id, dto, executor));
    }
}
