package com.example.arbor.dto.map;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Vigor;

import java.util.UUID;

/**
 * Payload do painel lateral do mapa — GET /api/map/trees/{treeId}/detail.
 * Mais rico que MapTreeDTO, mas sem histórico completo de registos.
 *
 * O campo currentRecord será injectado pelo serviço assim que a Pessoa 3
 * disponibilizar o RegistroResumoDTO. Por agora fica como Object (null-safe).
 */
public record MapTreeDetailDTO(
        UUID        id,
        String      codigo,
        String      nomeComum,
        String      especie,
        Double      lat,
        Double      lng,
        String      bairro,
        String      rua,
        String      referencia,
        EstadoGeral status,
        Vigor       vigor,
        String      observacoes,

        // TODO Pessoa 3: substituir Object por RegistroResumoDTO
        Object      currentRecord,
        String      fotoUrl
) {
    /** Construtor sem currentRecord — usado enquanto Pessoa 3 não entrega o contrato. */
    public MapTreeDetailDTO(Arvore a) {
        this(
                a.getId(),
                a.getCodigo(),
                a.getNomeComum(),
                a.getEspecie(),
                a.toLat(),
                a.toLng(),
                a.getBairro(),
                a.getRua(),
                a.getReferencia(),
                a.getEstadoGeral(),
                a.getVigor(),
                a.getObservacoes(),
                null,
                a.hasFoto() ? "/api/arvores/" + a.getId() + "/foto" : null
        );
    }
}