package com.example.arbor.controller;

import com.example.arbor.dto.request.RedefinirSenhaDTO;
import com.example.arbor.dto.request.SolicitarRecuperacaoDTO;
import com.example.arbor.dto.request.VerificarCodigoDTO;
import com.example.arbor.dto.response.MensagemResponseDTO;
import com.example.arbor.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recuperar-senha")
@CrossOrigin(origins = "*")
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService service;

    public RecuperacaoSenhaController(RecuperacaoSenhaService service) {
        this.service = service;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<MensagemResponseDTO> solicitarRecuperacao(@Valid @RequestBody SolicitarRecuperacaoDTO dto) {
        service.solicitarRecuperacao(dto);
        return ResponseEntity.ok(new MensagemResponseDTO(
                "Se o e-mail estiver cadastrado, um codigo de recuperacao sera enviado."));
    }

    @PostMapping("/verificar")
    public ResponseEntity<MensagemResponseDTO> verificarCodigo(@Valid @RequestBody VerificarCodigoDTO dto) {
        service.verificarCodigo(dto);
        return ResponseEntity.ok(new MensagemResponseDTO("Codigo valido. Prossiga para redefinir sua senha."));
    }

    @PostMapping("/redefinir")
    public ResponseEntity<MensagemResponseDTO> redefinirSenha(@Valid @RequestBody RedefinirSenhaDTO dto) {
        service.redefinirSenha(dto);
        return ResponseEntity.ok(new MensagemResponseDTO("Senha redefinida com sucesso."));
    }
}
