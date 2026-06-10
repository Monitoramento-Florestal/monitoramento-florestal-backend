package com.example.arbor.controller;

import com.example.arbor.dto.request.ArvoreRequestDTO;
import com.example.arbor.dto.response.ArvoreResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import com.example.arbor.service.ArvoreService;
import com.example.arbor.service.ExportacaoArvoresService;
import com.example.arbor.service.ExportacaoArvoresService.ExportacaoPreparada;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/arvores")
public class ArvoreController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArvoreController.class);

    private final ArvoreService arvoreService;
    private final ExportacaoArvoresService exportacaoArvoresService;

    public ArvoreController(
            ArvoreService arvoreService,
            ExportacaoArvoresService exportacaoArvoresService) {
        this.arvoreService = arvoreService;
        this.exportacaoArvoresService = exportacaoArvoresService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> listarTodas() {
        return ResponseEntity.ok(arvoreService.listarTodas());
    }

    @GetMapping("/exportacao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<StreamingResponseBody> exportar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam String formato,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        ExportacaoPreparada exportacao =
                exportacaoArvoresService.preparar(dataInicial, dataFinal, formato);
        LOGGER.info(
                "Exportacao de arvores solicitada: usuarioId={}, inicio={}, fim={}, formato={}, total={}",
                usuarioLogado != null ? usuarioLogado.getId() : null,
                dataInicial,
                dataFinal,
                exportacao.formato(),
                exportacao.totalRegistros());
        StreamingResponseBody body =
                outputStream -> exportacaoArvoresService.exportar(exportacao, outputStream);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(exportacao.nomeArquivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(exportacao.formato().mediaType()))
                .body(body);
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

    @GetMapping("/estado-geral/{estadoGeral}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarPorEstadoGeral(
            @PathVariable EstadoGeral estadoGeral
    ) {
        return ResponseEntity.ok(arvoreService.buscarPorEstadoGeral(estadoGeral));
    }

    @GetMapping("/vigor/{vigor}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarPorVigor(@PathVariable Vigor vigor) {
        return ResponseEntity.ok(arvoreService.buscarPorVigor(vigor));
    }

    @GetMapping("/problemas/copa/{problema}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarProblemaCopa(@PathVariable Problema problema) {
        return ResponseEntity.ok(arvoreService.buscarPorProblemaCopa(problema));
    }

    @GetMapping("/problemas/tronco/{problema}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarProblemaTronco(@PathVariable Problema problema) {
        return ResponseEntity.ok(arvoreService.buscarPorProblemaTronco(problema));
    }

    @GetMapping("/problemas/raiz/{problema}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<List<ArvoreResponseDTO>> buscarProblemaRaiz(@PathVariable Problema problema) {
        return ResponseEntity.ok(arvoreService.buscarPorProblemaRaiz(problema));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<ArvoreResponseDTO> cadastrar(
            @Valid @RequestBody ArvoreRequestDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(arvoreService.salvar(dto, usuarioLogado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR')")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        arvoreService.deletar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<ArvoreResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ArvoreRequestDTO dto,
            @AuthenticationPrincipal Usuario executor
    ) {
        return ResponseEntity.ok(arvoreService.atualizar(id, dto, executor));
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> buscarFoto(@PathVariable UUID id) {
        byte[] foto = arvoreService.getFoto(id);

        if (foto == null || foto.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String contentType = arvoreService.getFotoContentType(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        contentType != null ? contentType : "application/octet-stream"))
                .body(foto);
    }

    @PutMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public ResponseEntity<Void> uploadFoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Usuario executor) throws IOException {
        arvoreService.salvarFoto(id, file.getBytes(), file.getContentType(), executor);
        return ResponseEntity.noContent().build();
    }
}
