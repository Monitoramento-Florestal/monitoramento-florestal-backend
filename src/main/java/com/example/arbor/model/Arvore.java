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
public class Arvore extends DadosArvore{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_aprovacao", nullable = false)
    private LocalDate dataAprovacao = LocalDate.now();
}