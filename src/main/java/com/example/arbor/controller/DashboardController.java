package com.example.arbor.controller;

import com.example.arbor.dto.response.DashboardAdministrativoResponseDTO;
import com.example.arbor.dto.response.DashboardPesquisadorResponseDTO;
import com.example.arbor.dto.response.DashboardPublicoResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/publico")
    @PreAuthorize("hasRole('PUBLICO_GERAL')")
    public DashboardPublicoResponseDTO dashboardPublico() {
        return dashboardService.dashboardPublico();
    }

    @GetMapping("/pesquisador")
    @PreAuthorize("hasRole('PESQUISADOR')")
    public ResponseEntity<DashboardPesquisadorResponseDTO> dashboardPesquisador(
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                dashboardService.dashboardPesquisador(usuario.getId())
        );
    }

    @GetMapping("/gestor")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<DashboardAdministrativoResponseDTO> dashboardGestor(
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                dashboardService.dashboardAdministrativo(usuario.getId())
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DashboardAdministrativoResponseDTO> dashboardAdministrador(
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                dashboardService.dashboardAdministrativo(usuario.getId())
        );
    }
}
