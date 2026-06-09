package com.example.arbor.dto.map;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload mínimo para um pin no mapa.
 * Nunca carrega histórico, conflitos, manejo ou colecções — apenas o essencial
 * para renderizar o marcador e o tooltip de pré-visualização.
 *
 * ultimaMedicao: data do último registo aprovado.
 * TODO Pessoa 3: injectar valor real via RegistroResumoDTO quando disponível.
 * Por agora é sempre null — o campo já existe no contrato para não quebrar o frontend.
 */
public record MapTreeDTO(
        UUID          id,
        String        codigo,
        String        nomeComum,
        String        especie,
        Double        lat,
        Double        lng,
        EstadoGeral   status,
        LocalDateTime ultimaMedicao
) {
    /** Construtor a partir da entidade — ultimaMedicao null até Pessoa 3 entregar contrato. */
    public MapTreeDTO(Arvore a) {
        this(
                a.getId(),
                a.getCodigo(),
                a.getNomeComum(),
                a.getEspecie(),
                a.toLat(),
                a.toLng(),
                a.getEstadoGeral(),
                null  // ultimaMedicao — aguarda RegistroResumoDTO da Pessoa 3
        );
    }
}