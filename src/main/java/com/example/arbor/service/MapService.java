package com.example.arbor.service;

import com.example.arbor.dto.map.*;
import com.example.arbor.exception.RecursoNaoEncontradoException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.repository.ArvoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MapService {

    /** Limite máximo absoluto de pins individuais numa única resposta. */
    static final int MAP_TREES_MAX_LIMIT = 500;

    /**
     * Limite padrão quando o cliente não informa.
     * Razoável para renderização fluida na maioria dos dispositivos.
     */
    static final int MAP_TREES_DEFAULT_LIMIT = 200;

    /**
     * Acima deste total no bbox, a resposta passa para modo cluster,
     * independentemente do zoom declarado pelo cliente.
     */
    static final int MAP_CLUSTER_THRESHOLD = 200;

    private final ArvoreRepository arvoreRepository;

    public MapService(ArvoreRepository arvoreRepository) {
        this.arvoreRepository = arvoreRepository;
    }

    // ----------------------------------------------------------------
    // GET /api/map/trees
    // ----------------------------------------------------------------

    /**
     * Retorna pontos ou clusters de árvores para o viewport informado.
     *
     * Lógica de modo:
     *  - totalInView <= threshold E zoom >= 14  → modo "trees" (pins individuais)
     *  - totalInView >  threshold OU zoom < 14  → modo "cluster" (grid agrupado)
     *
     * O cliente pode forçar modo "trees" com limit <= threshold, mas o servidor
     * nunca retorna mais de MAP_TREES_MAX_LIMIT itens independentemente do pedido.
     */
    public MapTreesResponseDTO getMapTrees(
            double  minLng,
            double  minLat,
            double  maxLng,
            double  maxLat,
            Integer zoom,
            String  status,
            String  search,
            String  species,
            boolean includeCut,
            Integer limit) {

        validarBbox(minLng, minLat, maxLng, maxLat);

        int efectiveLimit = resolveLimit(limit);
        int efectiveZoom  = zoom != null ? zoom : 15;

        long totalInView = arvoreRepository.countWithinBbox(
                minLng, minLat, maxLng, maxLat,
                status, species, search, includeCut);

        boolean usarCluster = totalInView > MAP_CLUSTER_THRESHOLD || efectiveZoom < 14;

        if (usarCluster) {
            double gridSize = calcularGridSize(efectiveZoom);
            List<MapClusterDTO> clusters = arvoreRepository
                    .findClustersWithinBbox(
                            minLng,
                            minLat,
                            maxLng,
                            maxLat,
                            status,
                            species,
                            search,
                            includeCut,
                            gridSize)
                    .stream()
                    .map(row -> new MapClusterDTO(
                            ((Number) row[0]).doubleValue(),  // lat (AVG Y)
                            ((Number) row[1]).doubleValue(),  // lng (AVG X)
                            ((Number) row[2]).longValue()     // count
                    ))
                    .toList();
            return MapTreesResponseDTO.ofClusters(totalInView, clusters);
        }

        List<MapTreeDTO> items = arvoreRepository
                .findWithinBbox(minLng, minLat, maxLng, maxLat,
                        status, species, search, includeCut, efectiveLimit)
                .stream()
                .map(MapTreeDTO::new)
                .toList();

        return MapTreesResponseDTO.ofTrees(totalInView, items);
    }

    // ----------------------------------------------------------------
    // GET /api/map/trees/{treeId}/detail
    // ----------------------------------------------------------------

    /**
     * Detalhe leve para o painel lateral do mapa.
     * Não carrega histórico completo — apenas dados correntes da árvore.
     * O currentRecord será injectado aqui assim que a Pessoa 3 disponibilizar o contrato.
     */
    public MapTreeDetailDTO getMapTreeDetail(UUID treeId) {
        return arvoreRepository.findByIdAndAtivaTrue(treeId)
                .map(MapTreeDetailDTO::new)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Árvore não encontrada ou inativa: " + treeId));
    }

    // ----------------------------------------------------------------
    // Helpers privados
    // ----------------------------------------------------------------

    private void validarBbox(double minLng, double minLat, double maxLng, double maxLat) {
        if (minLng < -180 || maxLng > 180 || minLat < -90 || maxLat > 90) {
            throw new RequisicaoInvalidaException(
                    "Parâmetros de bbox fora dos limites geográficos válidos " +
                            "(lng: -180..180, lat: -90..90).");
        }
        if (minLng >= maxLng) {
            throw new RequisicaoInvalidaException(
                    "minLng deve ser menor que maxLng.");
        }
        if (minLat >= maxLat) {
            throw new RequisicaoInvalidaException(
                    "minLat deve ser menor que maxLat.");
        }
        // Evita bboxes absurdamente grandes que fariam full-scan da tabela
        double deltaLng = maxLng - minLng;
        double deltaLat = maxLat - minLat;
        if (deltaLng > 90 || deltaLat > 90) {
            throw new RequisicaoInvalidaException(
                    "Área do bbox muito grande. Máximo permitido: 90° por eixo. " +
                            "Aproxime o zoom antes de consultar.");
        }
    }

    private int resolveLimit(Integer requested) {
        if (requested == null || requested <= 0) return MAP_TREES_DEFAULT_LIMIT;
        return Math.min(requested, MAP_TREES_MAX_LIMIT);
    }

    /**
     * Calcula o tamanho da célula do grid de clusters em graus, proporcional ao zoom.
     * Zoom 1  → ~5° por célula  (visão continental)
     * Zoom 10 → ~0.1° por célula
     * Zoom 13 → ~0.01° por célula (bairro)
     */
    private double calcularGridSize(int zoom) {
        // 360° / 2^zoom aproxima o grau por tile; dividimos por 4 para células menores
        return 360.0 / Math.pow(2, zoom) / 4.0;
    }
}
