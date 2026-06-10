package com.example.arbor.model;

import com.example.arbor.model.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import static org.hibernate.type.SqlTypes.BINARY;
import org.locationtech.jts.geom.Point;

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

    @Column(name = "codigo", nullable = false, unique = true, updatable = false)
    private String codigo;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "especie", nullable = false)
    private String especie;

    @Column(name = "nome_comum")
    private String nomeComum;

    @Column(name = "localizacao", columnDefinition = "GEOMETRY(Point, 4326)")
    private Point localizacao;

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

    @Column(name = "observacoes")
    private String observacoes;

    @JdbcTypeCode(BINARY)
    @Column(name = "foto", columnDefinition = "BYTEA")
    @JsonIgnore
    private byte[] foto;

    @Column(name = "foto_content_type")
    private String fotoContentType;

    @Column(nullable = false)
    private Boolean ativa = true;

    /**
     * Retorna a latitude do ponto geográfico, ou null se não geolocalizada.
     * Use este método para popular campos lat em DTOs públicos.
     */
    public Double toLat() {
        return localizacao != null ? localizacao.getY() : null;
    }

    public Double toLng() {
        return localizacao != null ? localizacao.getX() : null;
    }


    public boolean hasFoto() {
        return foto != null && foto.length > 0;
    }
    public boolean isGeolocalizada() {
        return localizacao != null;
    }

    @PrePersist
    void preencherDataCadastro() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }
}
