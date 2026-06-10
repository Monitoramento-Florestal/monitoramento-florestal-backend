package com.example.arbor.dto.response;

import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.EstadoGeral;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroRecenteResponseDTO(
        UUID id,
        String codigo,
        String nomeComum,
        String especie,
        EstadoGeral estadoGeral,
        Double alturaColetada,
        Double dapColetada,
        Double copaColetada,
        LocalDateTime dataColeta,
        String pesquisadorNome
) {
    public RegistroRecenteResponseDTO(RegistroArvore registro) {
        this(
                registro.getId(),
                registro.getArvore() != null ? registro.getArvore().getCodigo() : null,
                registro.getArvore() != null ? registro.getArvore().getNomeComum() : null,
                registro.getEspecie() != null ? registro.getEspecie() : (registro.getArvore() != null ? registro.getArvore().getEspecie() : null),
                registro.getEstadoGeral(),
                registro.getAlturaColetada(),
                registro.getDapColetada(),
                registro.getCopaColetada(),
                registro.getDataColeta(),
                registro.getPesquisador() != null ? registro.getPesquisador().getNome() : null
        );
    }
}
