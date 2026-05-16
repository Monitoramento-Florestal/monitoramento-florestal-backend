package com.example.arbor.dto.response;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.CondicaoArvore;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.StatusRegistro;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroResponseDTO(
        UUID id,
        UsuarioResumoDTO pesquisador,
        LocalDateTime dataColeta,
        Arvore arvore,
        StatusRegistro status,
        UsuarioResumoDTO administradorResponsavel,
        LocalDateTime dataAnalise,
        String motivoRecusa,
        Double altura,
        CondicaoArvore condicao
) {
    public RegistroResponseDTO(RegistroArvore registro){
        this(
                registro.getId(),
                new UsuarioResumoDTO(registro.getPesquisador()),
                registro.getDataColeta(),
                registro.getArvore(),
                registro.getStatus(),
                registro.getAdministradorResponsavel() == null
                        ? null
                        : new UsuarioResumoDTO(registro.getAdministradorResponsavel()),
                registro.getDataAnalise(),
                registro.getMotivoRecusa(),
                registro.getAlturaColetada(),
                registro.getCondicaoColetada()
        );
    }
}
