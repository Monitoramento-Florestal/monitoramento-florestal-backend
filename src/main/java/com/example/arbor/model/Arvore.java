package com.example.arbor.model;

import com.example.arbor.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point; // Lembrete: import do JTS para o PostGIS

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Column(name = "especie", nullable = false)
    private String especie;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "rua")
    private String rua;

    @Column(name = "referencia")
    private String referencia;

    @OneToMany(mappedBy = "arvore", cascade = CascadeType.ALL)
    private List<RegistroArvore> registros = new ArrayList<>();

    @Column(nullable = false)
    private Double alturaAtual;

    @Column(nullable = false)
    private Double dapAtual;

    @Column(nullable = false)
    private Double copaAtual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_geral", nullable = false)
    private EstadoGeral estadoGeral;

    @Enumerated(EnumType.STRING)
    @Column(name = "vigor", nullable = false)
    private Vigor vigor;

    @ElementCollection
    @CollectionTable(name = "tb_registro_problemas_copa", joinColumns = @JoinColumn(name = "arvore_id"))
    @Column(name = "problema")
    @Enumerated(EnumType.STRING)
    private Set<Problema> problemasCopa;

    @ElementCollection
    @CollectionTable(name = "tb_registro_problemas_tronco", joinColumns = @JoinColumn(name = "arvore_id"))
    @Column(name = "problema")
    @Enumerated(EnumType.STRING)
    private Set<Problema> problemasTronco;

    @ElementCollection
    @CollectionTable(name = "tb_registro_problemas_raiz", joinColumns = @JoinColumn(name = "arvore_id"))
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

    @ElementCollection
    @CollectionTable(name = "tb_alvo_potencial", joinColumns = @JoinColumn(name = "arvore_id"))
    @Column(name = "alvo_potencial")
    @Enumerated(EnumType.STRING)
    private Set<AlvoPotencial> alvosPotenciais;

    @ElementCollection
    @CollectionTable(name = "tb_alvo_sensivel", joinColumns = @JoinColumn(name = "arvore_id"))
    @Column(name = "alvo_sensivel")
    @Enumerated(EnumType.STRING)
    private Set<AlvoSensivel> alvosSensiveis;

    @Embedded
    private Conflito conflito;

    @Embedded
    private Manejo manejo;

    //verificar
    @Column(name = "observacoes")
    private String observacoes;

    @Column(nullable = false)
    private Boolean ativa = true;

}
