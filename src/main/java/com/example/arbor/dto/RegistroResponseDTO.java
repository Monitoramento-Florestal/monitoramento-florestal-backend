package com.example.arbor.dto;

import com.example.arbor.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroResponseDTO(
        UUID id,
        Usuario pesquisador,
        LocalDateTime dataColeta,
        Arvore arvore,
        StatusRegistro status,
        Usuario administradorResponsavel,
        LocalDateTime dataAnalise,
        String motivoRecusa,
        Double altura,
        CondicaoArvore condicao
) {
    public RegistroResponseDTO(RegistroArvore registro){
        this(
                registro.getId(),
                registro.getPesquisador(),
                registro.getDataColeta(),
                registro.getArvore(),
                registro.getStatus(),
                registro.getAdministradorResponsavel(),
                registro.getDataAnalise(),
                registro.getMotivoRecusa(),
                registro.getAlturaColetada(),
                registro.getCondicaoColetada()
        );
    }
}
