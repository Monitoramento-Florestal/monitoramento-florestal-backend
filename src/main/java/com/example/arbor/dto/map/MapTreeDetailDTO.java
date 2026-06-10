package com.example.arbor.dto.map;

import com.example.arbor.dto.response.RegistroResponseDTO;
import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Vigor;

import java.util.UUID;

/**
 * Payload do painel lateral do mapa — GET /api/map/trees/{treeId}/detail.
 * Inclui currentRecord com os dados do último registro aprovado
 * (dimensões reais, condição, estrutura, conflitos, manejo).
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
        Double      alturaAtual,
        Double      dapAtual,
        Double      copaAtual,
        String      observacoes,
        RegistroResponseDTO currentRecord,
        String      fotoUrl
) {
    /** Construtor a partir da entidade, sem currentRecord (preenchido depois pelo service). */
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
                a.getAlturaAtual(),
                a.getDapAtual(),
                a.getCopaAtual(),
                a.getObservacoes(),
                null,
                a.hasFoto() ? "/api/arvores/" + a.getId() + "/foto" : null
        );
    }
}
