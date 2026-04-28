package com.example.arbor.controller;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.StatusRegistro;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.RegistroArvoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/registros-arvore")
@RequiredArgsConstructor

public class RegistroArvoreController {
    private final RegistroArvoreService registroService;

    @PostMapping
    public ResponseEntity<RegistroArvore> cadastrar(@RequestBody RegistroArvore registro){
        return ResponseEntity.ok(registroService.cadastrar(registro));
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<Arvore> aprovar(@PathVariable UUID id, @RequestBody Usuario admin){
        return ResponseEntity.ok(registroService.aprovarRegistro(id, admin));
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<RegistroArvore> recusar(@PathVariable UUID id,
                                                  @RequestParam String motivo,
                                                  @RequestBody Usuario admin){
        return ResponseEntity.ok(registroService.recusarRegistro(id, motivo, admin));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RegistroArvore>> buscarPorStatus(@PathVariable StatusRegistro status){
        return ResponseEntity.ok(registroService.filtrarPorStatus(status));
    }

    @GetMapping("/pesquisador/{id}")
    public ResponseEntity<List<RegistroArvore>> buscarPorPesquisador(@PathVariable UUID id){
        return ResponseEntity.ok(registroService.filtrarPorPesquisador(id));
    }

    @GetMapping("/pesquisador/{id}/status/{status}")
    public ResponseEntity<List<RegistroArvore>> buscarPorStatusEPesquisador(@PathVariable UUID id,
                                                                            @PathVariable StatusRegistro status){
        return ResponseEntity.ok(registroService.filtrarPorStatusEPesquisador(status, id));
    }
}
