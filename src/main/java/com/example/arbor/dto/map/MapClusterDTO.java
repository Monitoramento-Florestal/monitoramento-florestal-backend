package com.example.arbor.dto.map;

/**
 * Representa um cluster de árvores para zooms baixos.
 * O centróide lat/lng é o ponto central do grupo de árvores.
 * Usado quando totalInView ultrapassa MAP_TREES_CLUSTER_THRESHOLD.
 */
public record MapClusterDTO(
        Double lat,
        Double lng,
        long   count
) {}