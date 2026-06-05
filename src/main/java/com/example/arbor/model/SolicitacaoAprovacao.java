package com.example.arbor.model;

import com.example.arbor.model.enums.StatusRegistro;
import com.example.arbor.model.enums.TipoSolicitacao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_solicitacao_aprovacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SolicitacaoAprovacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSolicitacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRegistro status;

    @Column(name = "data_submissao", nullable = false)
    private LocalDateTime dataSubmissao;

    @Column(name = "pesquisador_id", nullable = false)
    private UUID pesquisadorId;

    @Column(name = "data_revisao")
    private LocalDateTime dataRevisao;

    @Column(name = "revisor_id")
    private UUID revisorId;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    private UUID arvoreId;

    private UUID registroId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private PropostaArvore propostaArvore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private PropostaRegistro propostaRegistro;
}