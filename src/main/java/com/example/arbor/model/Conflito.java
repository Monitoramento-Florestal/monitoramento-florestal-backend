package com.example.arbor.model;

import com.example.arbor.model.enums.ConflitoIluminacao;
import com.example.arbor.model.enums.DanoCalcada;
import com.example.arbor.model.enums.NivelConflito;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conflito {

    @Enumerated(EnumType.STRING)
    private NivelConflito fiacao;

    @Enumerated(EnumType.STRING)
    private DanoCalcada calcada;

    @Enumerated(EnumType.STRING)
    private ConflitoIluminacao iluminacao;

    @Enumerated(EnumType.STRING)
    private NivelConflito edificacao;
}
