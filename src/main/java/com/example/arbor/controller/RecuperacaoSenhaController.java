package com.example.arbor.controller;

import com.example.arbor.dto.request.RedefinirSenhaDTO;
import com.example.arbor.dto.request.SolicitarRecuperacaoDTO;
import com.example.arbor.dto.request.VerificarCodigoDTO;
import com.example.arbor.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recuperar-senha")
@CrossOrigin(origins = "*")
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService service;

    public RecuperacaoSenhaController(RecuperacaoSenhaService service) {
        this.service = service;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<String> solicitarRecuperacao(@Valid @RequestBody SolicitarRecuperacaoDTO dto) {
        service.solicitarRecuperacao(dto);
        return ResponseEntity.ok("Código de recuperação enviado para o e-mail informado.");
    }

    @PostMapping("/verificar")
    public ResponseEntity<String> verificarCodigo(@Valid @RequestBody VerificarCodigoDTO dto) {
        service.verificarCodigo(dto);
        return ResponseEntity.ok("Código válido. Prossiga para redefinir sua senha.");
    }
    
    @PostMapping("/redefinir")
    public ResponseEntity<String> redefinirSenha(@Valid @RequestBody RedefinirSenhaDTO dto) {
        service.redefinirSenha(dto);
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}
