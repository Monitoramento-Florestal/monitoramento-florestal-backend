package com.example.arbor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_registro_arvore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RegistroArvore extends DadosArvore{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRegistro status;

    @Column(name = "motivo_recusa")
    private String motivoRecusa;

    @ManyToOne
    @JoinColumn(name = "pesquisador_id", nullable = false)
    private Usuario pesquisador;

    @ManyToOne
    @JoinColumn(name = "administrador_id")
    private Usuario administradorResponsavel;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;
}
