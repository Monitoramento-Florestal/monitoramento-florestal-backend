package com.example.arbor.dto;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.CondicaoArvore;
import java.time.LocalDate;
import java.util.UUID;

public record ArvoreResponseDTO(
        UUID id,
        String especie,
        Double altura,
        CondicaoArvore condicao,
        Double latitude,
        Double longitude
) {
    public ArvoreResponseDTO(Arvore arvore) {
        this(
                arvore.getId(),
                arvore.getEspecie(),
                arvore.getAlturaAtual(),
                arvore.getCondicaoAtual(),
                arvore.getLocalizacao().getY(), // Latitude
                arvore.getLocalizacao().getX()  // Longitude
        );
    }
}