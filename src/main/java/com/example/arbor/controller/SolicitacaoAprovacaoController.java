package com.example.arbor.controller;

import com.example.arbor.dto.request.AprovacaoCriacaoArvoreRequestDTO;
import com.example.arbor.dto.request.AprovacaoCriacaoRegistroRequestDTO;
import com.example.arbor.dto.request.AprovacaoEdicaoRegistroRequestDTO;
import com.example.arbor.dto.request.RecusarAprovacaoRequestDTO;
import com.example.arbor.dto.response.AcaoAprovacaoResponseDTO;
import com.example.arbor.dto.response.DetalheAprovacaoResponseDTO;
import com.example.arbor.dto.response.ListItemAprovacaoResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.enums.TipoSolicitacao;
import com.example.arbor.service.SolicitacaoAprovacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approval-requests")
@RequiredArgsConstructor
public class SolicitacaoAprovacaoController {

    private final SolicitacaoAprovacaoService solicitacaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public List<ListItemAprovacaoResponseDTO> listar(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) TipoSolicitacao tipo,
            @RequestParam(required = false) StatusRegistro status,
            @RequestParam(required = false) UUID pesquisadorId,
            @RequestParam(required = false) LocalDateTime dataSubmissao,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return solicitacaoService.listar(id, tipo, status, pesquisadorId, dataSubmissao, usuario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public DetalheAprovacaoResponseDTO detalhar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuario
    ){
        return solicitacaoService.buscarPorId(id, usuario);
    }

    @PostMapping("/criacao-arvore")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public UUID criarArvore(
            @RequestBody @Valid AprovacaoCriacaoArvoreRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ){
        return solicitacaoService
                .criarSolicitacaoCriacaoArvore(dto, usuario);
    }

    @PostMapping("/criacao-registro")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public UUID criarRegistro(
            @RequestBody @Valid AprovacaoCriacaoRegistroRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ){
        return solicitacaoService
                .criarSolicitacaoCriacaoRegistro(dto, usuario);
    }

    @PostMapping("/edicao-registro")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR')")
    public UUID editarRegistro(
            @RequestBody @Valid AprovacaoEdicaoRegistroRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ){
        return solicitacaoService
                .criarSolicitacaoEdicaoRegistro(dto, usuario);
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR')")
    public AcaoAprovacaoResponseDTO aprovar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario gestor
    ){
        return solicitacaoService.aprovar(id, gestor);
    }

    @PostMapping("/{id}/rejeitar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR')")
    public AcaoAprovacaoResponseDTO recusar(
            @PathVariable UUID id,
            @RequestBody @Valid RecusarAprovacaoRequestDTO dto,
            @AuthenticationPrincipal Usuario gestor
    ){
        return solicitacaoService.recusar(
                id,
                gestor,
                dto
        );
    }
}
