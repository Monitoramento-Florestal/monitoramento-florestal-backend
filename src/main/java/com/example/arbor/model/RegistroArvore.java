package com.example.arbor.model;

import com.example.arbor.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.Set;
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

    @Column(name = "especie")
    private String especie;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "rua")
    private String rua;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "localizacao_nova", columnDefinition = "GEOMETRY(Point, 4326)")
    private Point localizacaoNova;

    @ManyToOne
    @JoinColumn(name = "pesquisador", nullable = false)
    private Usuario pesquisador;

    @Column(name = "data_coleta")
    private LocalDateTime dataColeta;

    @Column(name = "versao", nullable = false)
    private Integer versao;

    @Column(name = "registro_origem_id")
    private UUID registroOrigemId;

    @ManyToOne
    @JoinColumn(name = "arvore")
    private Arvore arvore;

    @ManyToOne
    @JoinColumn(name = "administrador")
    private Usuario administradorResponsavel;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @Column(name = "motivo_recusa")
    private String motivoRecusa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRegistro status;

    @Column(name = "altura_coletada")
    private Double alturaColetada;

    @Column(name = "dap_coletada")
    private Double dapColetada;

    @Column(name = "copa_coletada")
    private Double copaColetada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_geral")
    private EstadoGeral estadoGeral;

    @Enumerated(EnumType.STRING)
    @Column(name = "vigor")
    private Vigor vigor;

    @ElementCollection(targetClass = Problema.class)
    @CollectionTable(name = "tb_registro_problemas_copa", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "problema")
    @Enumerated(EnumType.STRING)
    private Set<Problema> problemasCopa;

    @ElementCollection(targetClass = Problema.class)
    @CollectionTable(name = "tb_registro_problemas_tronco", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "problema")
    @Enumerated(EnumType.STRING)
    private Set<Problema> problemasTronco;

    @ElementCollection(targetClass = Problema.class)
    @CollectionTable(name = "tb_registro_problemas_raiz", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "problema")
    @Enumerated(EnumType.STRING)
    private Set<Problema> problemasRaiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "estrutura_tronco")
    private EstruturaTronco estruturaTronco;

    @Enumerated(EnumType.STRING)
    @Column(name = "estrutura_base")
    private EstruturaBase estruturaBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "estrutura_copa")
    private EstruturaCopa estruturaCopa;

    @Enumerated(EnumType.STRING)
    @Column(name = "inclinacao")
    private InclinacaoTronco inclinacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancoragem")
    private AncoragemRadicular ancoragem;

    @Enumerated(EnumType.STRING)
    @Column(name = "fluxo_pedestre")
    private FluxoPedestre fluxoPedestre;

    @Enumerated(EnumType.STRING)
    @Column(name = "fluxo_automovel")
    private FluxoAutomovel fluxoAutomovel;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_via")
    private TipoVia tipoVia;

    @ElementCollection(targetClass = AlvoPotencial.class)
    @CollectionTable(name = "tb_alvo_potencial", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "alvo_potencial")
    @Enumerated(EnumType.STRING)
    private Set<AlvoPotencial> alvosPotenciais;

    @ElementCollection(targetClass = AlvoSensivel.class)
    @CollectionTable(name = "tb_alvo_sensivel", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "alvo_sensivel")
    @Enumerated(EnumType.STRING)
    private Set<AlvoSensivel> alvosSensiveis;

    @Embedded
    private Conflito conflito;

    @Embedded
    private Manejo manejo;

    @Column(name = "observacoes")
    private String observacoes;

    public Double toLatNova() {
        return localizacaoNova != null ? localizacaoNova.getY() : null;
    }

    public Double toLngNova() {
        return localizacaoNova != null ? localizacaoNova.getX() : null;
    }
}
