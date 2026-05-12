package com.example.arbor.dto;

import com.example.arbor.model.*;

import java.time.LocalDateTime;

public record RegistroNovaArvoreResponseDTO(
        Usuario pesquisador,
        LocalDateTime dataColeta,
        String especie,
        Double altura,
        CondicaoArvore condicao,
        Double latitude,
        Double longitude
) {
    public RegistroNovaArvoreResponseDTO(RegistroArvore registro) {
        this(
                registro.getPesquisador(),
                registro.getDataColeta(),
                registro.getEspecieNova(),
                registro.getAlturaColetada(),
                registro.getCondicaoColetada(),
                registro.getLocalizacaoNova().getY(),
                registro.getLocalizacaoNova().getX()

        );
    }
}

