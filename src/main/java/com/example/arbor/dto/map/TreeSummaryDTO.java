package com.example.arbor.dto.map;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload de uma linha na listagem administrativa — GET /api/trees (paginado).
 * Mais leve que ArvoreResponseDTO; sem colecções de problemas, conflitos ou manejo.
 *
 * ultimaMedicao: data do último registo aprovado.
 * TODO Pessoa 3: injectar valor real via RegistroResumoDTO quando disponível.
 */
public record TreeSummaryDTO(
        UUID          id,
        String        codigo,
        String        especie,
        String        nomeComum,
        Double        lat,
        Double        lng,
        String        bairro,
        String        rua,
        EstadoGeral   status,
        Boolean       ativa,
        LocalDateTime ultimaMedicao
) {
    /** Construtor a partir da entidade — ultimaMedicao null até Pessoa 3 entregar contrato. */
    public TreeSummaryDTO(Arvore a) {
        this(
                a.getId(),
                a.getCodigo(),
                a.getEspecie(),
                a.getNomeComum(),
                a.toLat(),
                a.toLng(),
                a.getBairro(),
                a.getRua(),
                a.getEstadoGeral(),
                a.getAtiva(),
                null  // ultimaMedicao — aguarda RegistroResumoDTO da Pessoa 3
        );
    }
}