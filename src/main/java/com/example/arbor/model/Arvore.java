package com.example.arbor.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point; // Lembrete: import do JTS para o PostGIS

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_arvore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Arvore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String especie;

    @Column(nullable = false)
    private Double altura;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao", nullable = false)
    private CondicaoArvore condicao;

    //nesse modelo um novo atributo é adicionado
    @Enumerated(EnumType.STRING)
    @Column(name = "status_registro", nullable = false)
    private StatusRegistro status_registro;

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro = LocalDate.now();

    @Column(name = "localizacao", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point localizacao;
}