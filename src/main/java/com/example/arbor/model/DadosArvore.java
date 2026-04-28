package com.example.arbor.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

@Getter
@Setter
@MappedSuperclass
public abstract class DadosArvore {

    @Column(nullable = false)
    private String especie;

    @Column(nullable = false)
    private Double altura;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao", nullable = false)
    private CondicaoArvore condicao;

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro = LocalDate.now();

    @Column(name = "localizacao", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point localizacao;
}
