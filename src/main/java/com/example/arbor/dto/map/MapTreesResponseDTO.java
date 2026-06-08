package com.example.arbor.dto.map;

import java.util.List;

/**
 * Envelope de resposta do GET /api/map/trees.
 *
 * mode  = "trees"   → items contém MapTreeDTO individuais
 * mode  = "cluster" → clusters contém MapClusterDTO agrupados por grid
 *
 * totalInView é sempre preenchido — o frontend usa para saber se há mais
 * árvores além do limite retornado (e exibir aviso ao utilizador).
 */
public record MapTreesResponseDTO(
        String            mode,
        long              totalInView,
        List<MapTreeDTO>  items,
        List<MapClusterDTO> clusters
) {

    /** Construtor de conveniência para o modo "trees". */
    public static MapTreesResponseDTO ofTrees(long totalInView, List<MapTreeDTO> items) {
        return new MapTreesResponseDTO("trees", totalInView, items, List.of());
    }

    /** Construtor de conveniência para o modo "cluster". */
    public static MapTreesResponseDTO ofClusters(long totalInView, List<MapClusterDTO> clusters) {
        return new MapTreesResponseDTO("cluster", totalInView, List.of(), clusters);
    }
}