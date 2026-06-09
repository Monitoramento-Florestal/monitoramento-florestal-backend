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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints de leitura geoespacial do mapa.
 *
 * Regra de acesso:
 *  - Endpoints publicos de leitura, acessiveis sem autenticacao.
 *  - O dashboard autenticado pode consumir os mesmos endpoints.
 */
@Tag(name = "Mapa", description = "Endpoints geoespaciais de leitura do mapa de arvores")
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
     * Endpoint publico.
     */
    @Operation(
            summary = "Listar arvores no viewport do mapa",
            description = """
                    Retorna pins individuais (mode=trees) ou clusters (mode=cluster) conforme
                    o total de arvores no bbox e o nivel de zoom.
                    Regra: totalInView <= 200 e zoom >= 14 -> trees; caso contrario -> cluster.
                    Arvores inativas sao excluidas por defeito (includeCut=false).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = MapTreesResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parametros de bbox invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "N/A para endpoint publico",
                    content = @Content)
    })
    @GetMapping("/trees")
    public ResponseEntity<MapTreesResponseDTO> getMapTrees(
            @Parameter(description = "Longitude minima do viewport (obrigatorio)", example = "-34.92")
            @RequestParam double minLng,

            @Parameter(description = "Latitude minima do viewport (obrigatorio)", example = "-8.10")
            @RequestParam double minLat,

            @Parameter(description = "Longitude maxima do viewport (obrigatorio)", example = "-34.85")
            @RequestParam double maxLng,

            @Parameter(description = "Latitude maxima do viewport (obrigatorio)", example = "-8.02")
            @RequestParam double maxLat,

            @Parameter(description = "Nivel de zoom do mapa (default 15). Abaixo de 14 forca modo cluster.")
            @RequestParam(required = false) Integer zoom,

            @Parameter(description = "Filtro por EstadoGeral (ex: OTIMO, BOM, REGULAR, RUIM, MORTA)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Busca textual em codigo, especie e nomeComum")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filtro parcial por especie (ex: Ficus)")
            @RequestParam(required = false) String species,

            @Parameter(description = "Se true, inclui arvores com estado MORTA (default false)")
            @RequestParam(required = false, defaultValue = "false") boolean includeCut,

            @Parameter(description = "Maximo de pins individuais (default 200, maximo 500)")
            @RequestParam(required = false) Integer limit) {

        return ResponseEntity.ok(
                mapService.getMapTrees(
                        minLng, minLat, maxLng, maxLat,
                        zoom, status, search, species, includeCut, limit));
    }

    /**
     * GET /api/map/trees/{treeId}/detail
     *
     * Endpoint publico.
     */
    @Operation(
            summary = "Detalhe da arvore para o painel lateral do mapa",
            description = """
                    Retorna dados correntes da arvore sem historico completo de registros.
                    Inclui currentRecord resumido (null ate a entrega do contrato apropriado).
                    Retorna 404 se a arvore nao existir ou estiver inativa.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = MapTreeDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "N/A para endpoint publico",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Arvore nao encontrada ou inativa",
                    content = @Content)
    })
    @GetMapping("/trees/{treeId}/detail")
    public ResponseEntity<MapTreeDetailDTO> getMapTreeDetail(
            @Parameter(description = "UUID da arvore", required = true)
            @PathVariable UUID treeId) {
        return ResponseEntity.ok(mapService.getMapTreeDetail(treeId));
    }
}
