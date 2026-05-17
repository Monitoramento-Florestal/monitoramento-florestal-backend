package com.example.arbor.dto.response;

import com.example.arbor.dto.resumo.UsuarioResumoDTO;
import com.example.arbor.model.CondicaoArvore;
import com.example.arbor.model.RegistroArvore;

import java.time.LocalDateTime;

public record RegistroNovaArvoreResponseDTO(
        UsuarioResumoDTO pesquisador,
        LocalDateTime dataColeta,
        String especie,
        Double altura,
        CondicaoArvore condicao,
        Double latitude,
        Double longitude
) {
    public RegistroNovaArvoreResponseDTO(RegistroArvore registro) {
        this(
                new UsuarioResumoDTO(registro.getPesquisador()),
                registro.getDataColeta(),
                registro.getEspecieNova(),
                registro.getAlturaColetada(),
                registro.getCondicaoColetada(),
                registro.getLocalizacaoNova().getY(),
                registro.getLocalizacaoNova().getX()

        );
    }
}
