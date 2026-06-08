package com.example.arbor.controller;

import com.example.arbor.dto.map.MapTreeDetailDTO;
import com.example.arbor.dto.map.MapTreesResponseDTO;
import com.example.arbor.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints de leitura geoespacial do mapa.
 *
 * Regra de acesso (alinhada com Pessoa 1):
 *  - Todos os endpoints exigem autenticação (qualquer role incluindo PUBLICO_GERAL).
 *  - Se o produto decidir tornar o mapa público (sem token), a Pessoa 1 adiciona
 *    .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll() na SecurityConfig
 *    e os @PreAuthorize abaixo podem ser removidos.
 */
@Tag(name = "Mapa", description = "Endpoints geoespaciais de leitura do mapa de árvores")
@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "*")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    /**
     * GET /api/map/trees
     *
     * Roles permitidas: PUBLICO_GERAL, PESQUISADOR, GESTOR, ADMINISTRADOR.
     */
    @Operation(
            summary = "Listar árvores no viewport do mapa",
            description = """
                    Retorna pins individuais (mode=trees) ou clusters (mode=cluster) consoante
                    o total de árvores no bbox e o nível de zoom.
                    Regra: totalInView <= 200 E zoom >= 14 → trees; caso contrário → cluster.
                    Árvores inativas são excluídas por defeito (includeCut=false).
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = MapTreesResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de bbox inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content)
    })
    @GetMapping("/trees")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<MapTreesResponseDTO> getMapTrees(
            @Parameter(description = "Longitude mínima do viewport (obrigatório)", example = "-34.92")
            @RequestParam double minLng,

            @Parameter(description = "Latitude mínima do viewport (obrigatório)", example = "-8.10")
            @RequestParam double minLat,

            @Parameter(description = "Longitude máxima do viewport (obrigatório)", example = "-34.85")
            @RequestParam double maxLng,

            @Parameter(description = "Latitude máxima do viewport (obrigatório)", example = "-8.02")
            @RequestParam double maxLat,

            @Parameter(description = "Nível de zoom do mapa (default 15). Abaixo de 14 força modo cluster.")
            @RequestParam(required = false) Integer zoom,

            @Parameter(description = "Filtro por EstadoGeral (ex: OTIMO, BOM, REGULAR, RUIM, MORTA)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Busca textual em codigo, especie e nomeComum")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filtro parcial por espécie (ex: Ficus)")
            @RequestParam(required = false) String species,

            @Parameter(description = "Se true, inclui árvores com estado MORTA (default false)")
            @RequestParam(required = false, defaultValue = "false") boolean includeCut,

            @Parameter(description = "Máximo de pins individuais (default 200, máximo 500)")
            @RequestParam(required = false) Integer limit) {

        return ResponseEntity.ok(
                mapService.getMapTrees(
                        minLng, minLat, maxLng, maxLat,
                        zoom, status, search, species, includeCut, limit));
    }

    /**
     * GET /api/map/trees/{treeId}/detail
     *
     * Roles permitidas: PUBLICO_GERAL, PESQUISADOR, GESTOR, ADMINISTRADOR.
     */
    @Operation(
            summary = "Detalhe da árvore para o painel lateral do mapa",
            description = """
                    Retorna dados correntes da árvore sem histórico completo de registos.
                    Inclui currentRecord resumido (null até Pessoa 3 entregar o contrato).
                    Retorna 404 se a árvore não existir ou estiver inativa.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = MapTreeDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Árvore não encontrada ou inativa",
                    content = @Content)
    })
    @GetMapping("/trees/{treeId}/detail")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GESTOR','PESQUISADOR','PUBLICO_GERAL')")
    public ResponseEntity<MapTreeDetailDTO> getMapTreeDetail(
            @Parameter(description = "UUID da árvore", required = true)
            @PathVariable UUID treeId) {
        return ResponseEntity.ok(mapService.getMapTreeDetail(treeId));
    }
}
