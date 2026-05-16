package com.example.arbor.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point; // Lembrete: import do JTS para o PostGIS

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private Double alturaAtual;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao", nullable = false)
    private CondicaoArvore condicaoAtual;

    @Column(name = "localizacao", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point localizacao;

    @OneToMany(mappedBy = "arvore", cascade = CascadeType.ALL)
    private List<RegistroArvore> registros = new ArrayList<>();

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "rua")
    private String rua;

    @Column(name = "referencia")
    private String referencia;

}
