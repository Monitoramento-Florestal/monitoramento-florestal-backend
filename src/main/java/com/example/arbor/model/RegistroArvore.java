package com.example.arbor.model;

import jakarta.persistence.*;
import lombok.*;

import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_registro_arvore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RegistroArvore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pesquisador_id", nullable = false)
    private Usuario pesquisador;

    @Column(name = "data_coleta")
    private LocalDateTime dataColeta;

    @ManyToOne
    @JoinColumn(name = "arvore_id")
    private Arvore arvore;

    @Column(name = "especie_nova")
    private String especieNova;

    @Column(name = "localizacao_nova", columnDefinition = "geometry(Point, 4326)")
    private Point localizacaoNova;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRegistro status;

    //VALIDAÇÃO DA SOLICITAÇÃO

    @ManyToOne
    @JoinColumn(name = "administrador_id")
    private Usuario administradorResponsavel;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    private String motivoRecusa; //

    //DADOS MUTÁVEIS DA ÁRVORE COLETADA (vale lembrar que provavelmente os atributos aumentarão posteriormente)

    @Column(name = "altura_coletada")
    private Double alturaColetada;

    @Enumerated(EnumType.STRING)
    private CondicaoArvore condicaoColetada;
}
