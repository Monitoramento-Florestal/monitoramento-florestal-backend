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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/recuperar-senha")
@CrossOrigin(origins = "*")
public class RecuperacaoSenhaController {

    private static final long RESET_REQUEST_MIN_RESPONSE_MILLIS = 500;
    private static final long RESET_REQUEST_JITTER_MILLIS = 150;

    private final RecuperacaoSenhaService service;

    public RecuperacaoSenhaController(RecuperacaoSenhaService service) {
        this.service = service;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<MensagemResponseDTO> solicitarRecuperacao(@Valid @RequestBody SolicitarRecuperacaoDTO dto) {
        long startedAt = System.nanoTime();
        try {
            service.solicitarRecuperacao(dto);
        } finally {
            aguardarJanelaMinima(startedAt);
        }

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

    private void aguardarJanelaMinima(long startedAt) {
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        long targetMillis = RESET_REQUEST_MIN_RESPONSE_MILLIS
                + ThreadLocalRandom.current().nextLong(RESET_REQUEST_JITTER_MILLIS + 1);
        long remainingMillis = targetMillis - elapsedMillis;

        if (remainingMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(remainingMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
