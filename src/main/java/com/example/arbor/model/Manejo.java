package com.example.arbor.model;

import com.example.arbor.model.enums.AcaoManejo;
import com.example.arbor.model.enums.Prioridade;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Set;

@Embeddable
public class Manejo {

    @ElementCollection(targetClass = AcaoManejo.class)
    @Enumerated(EnumType.STRING)
    private Set<AcaoManejo> acoes;

    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;
}
