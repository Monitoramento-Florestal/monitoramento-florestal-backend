package com.example.arbor.integration;

import com.example.arbor.dto.map.MapTreeDetailDTO;
import com.example.arbor.dto.map.MapTreeDTO;
import com.example.arbor.dto.map.MapTreesResponseDTO;
import com.example.arbor.dto.request.ArvoreRequestDTO;
import com.example.arbor.dto.response.ArvoreResponseDTO;
import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.*;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.UsuarioRepository;
import com.example.arbor.service.ArvoreService;
import com.example.arbor.service.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para os endpoints de mapa e listagem de árvores.
 * Usa o banco PostgreSQL/PostGIS local — rollback automático via @Transactional.
 */
@Transactional
class MapIT extends BaseIntegrationTest {

    @Autowired
    private ArvoreService arvoreService;

    @Autowired
    private MapService mapService;

    @Autowired
    private ArvoreRepository arvoreRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Coordenadas dentro do bbox de teste (Recife - Boa Viagem)
    private static final double LAT_DENTRO  = -8.1200;
    private static final double LNG_DENTRO  = -34.9000;

    // Coordenadas fora do bbox de teste (São Paulo)
    private static final double LAT_FORA    = -23.5505;
    private static final double LNG_FORA    = -46.6333;

    // BBox de teste: cobre só Recife/Boa Viagem
    private static final double MIN_LNG = -35.0;
    private static final double MIN_LAT = -8.2;
    private static final double MAX_LNG = -34.8;
    private static final double MAX_LAT = -8.0;

    private Usuario gestor;

    @BeforeEach
    void setUp() {
        gestor = usuarioRepository.findAll().stream()
                .filter(u -> u.getPerfilAcesso() == Perfil.ADMINISTRADOR
                        || u.getPerfilAcesso() == Perfil.GESTOR)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhum utilizador GESTOR/ADMINISTRADOR encontrado. " +
                                "Verifica o DataSeeder."));
    }

    // ----------------------------------------------------------------
    // GET /api/map/trees — bbox contendo árvores
    // ----------------------------------------------------------------

    @Test
    void getMapTreesDeveRetornarArvoreDentroDoBbox() {
        criarArvore("Ficus benjamina", LAT_DENTRO, LNG_DENTRO);

        MapTreesResponseDTO resposta = mapService.getMapTrees(
                MIN_LNG, MIN_LAT, MAX_LNG, MAX_LAT,
                15, null, null, null, false, null);

        assertThat(resposta.totalInView()).isGreaterThanOrEqualTo(1L);
        assertThat(resposta.mode()).isEqualTo("trees");
        assertThat(resposta.items()).isNotEmpty();
    }

    @Test
    void getMapTreesNaoDeveRetornarArvoreForaDoBbox() {
        criarArvore("Ipê Amarelo", LAT_FORA, LNG_FORA);

        MapTreesResponseDTO resposta = mapService.getMapTrees(
                MIN_LNG, MIN_LAT, MAX_LNG, MAX_LAT,
                15, null, null, null, false, null);

        boolean presente = resposta.items().stream()
                .anyMatch(t -> "Ipê Amarelo".equals(t.especie()));
        assertThat(presente).isFalse();
    }

    // ----------------------------------------------------------------
    // Resposta JSON não contém JTS — sempre lat/lng como Double
    // ----------------------------------------------------------------

    @Test
    void getMapTreesRespostaDeveConterLatLngComoDoubleNuncaJts() {
        criarArvore("Mangifera indica", LAT_DENTRO, LNG_DENTRO);

        MapTreesResponseDTO resposta = mapService.getMapTrees(
                MIN_LNG, MIN_LAT, MAX_LNG, MAX_LAT,
                15, null, null, null, false, null);

        for (MapTreeDTO item : resposta.items()) {
            assertThat(item.lat()).isNotNull();
            assertThat(item.lng()).isNotNull();
            assertThat(item.lat()).isInstanceOf(Double.class);
            assertThat(item.lng()).isInstanceOf(Double.class);
            assertThat(item.lat()).isBetween(-90.0, 90.0);
            assertThat(item.lng()).isBetween(-180.0, 180.0);
        }
    }

    // ----------------------------------------------------------------
    // Limite máximo respeitado
    // ----------------------------------------------------------------

    @Test
    void getMapTreesDeveRespeitarLimiteMaximo() {
        for (int i = 0; i < 10; i++) {
            criarArvore("Especie " + i,
                    LAT_DENTRO + (i * 0.001),
                    LNG_DENTRO + (i * 0.001));
        }

        MapTreesResponseDTO resposta = mapService.getMapTrees(
                MIN_LNG, MIN_LAT, MAX_LNG, MAX_LAT,
                15, null, null, null, false, 3);

        if ("trees".equals(resposta.mode())) {
            assertThat(resposta.items().size()).isLessThanOrEqualTo(3);
        }
        assertThat(resposta.totalInView()).isGreaterThanOrEqualTo(10L);
    }

    @Test
    void getMapTreesDeveRespeitarLimiteAbsoluto500() {
        MapTreesResponseDTO resposta = mapService.getMapTrees(
                MIN_LNG, MIN_LAT, MAX_LNG, MAX_LAT,
                15, null, null, null, false, 9999);

        assertThat(resposta).isNotNull();
        assertThat(resposta.mode()).isIn("trees", "cluster");
    }

    // ----------------------------------------------------------------
    // Árvores inativas excluídas por defeito
    // ----------------------------------------------------------------

    @Test
    void getMapTreesNaoDeveRetornarArvoresInativasComIncludeCutFalse() {
        UUID id = criarArvore("Arvore Inativa", LAT_DENTRO, LNG_DENTRO);
        arvoreService.deletar(id, gestor);

        MapTreesResponseDTO resposta = mapService.getMapTrees(
                MIN_LNG, MIN_LAT, MAX_LNG, MAX_LAT,
                15, null, null, null, false, null);

        boolean inativaPresente = resposta.items().stream()
                .anyMatch(t -> "Arvore Inativa".equals(t.especie()));
        assertThat(inativaPresente).isFalse();
    }

    // ----------------------------------------------------------------
    // GET /api/map/trees/{treeId}/detail
    // ----------------------------------------------------------------

    @Test
    void getMapTreeDetailDeveRetornarArvorePorId() {
        UUID id = criarArvore("Ficus benjamina", LAT_DENTRO, LNG_DENTRO);

        MapTreeDetailDTO detalhe = mapService.getMapTreeDetail(id);

        assertThat(detalhe.id()).isEqualTo(id);
        assertThat(detalhe.especie()).isEqualTo("Ficus benjamina");
        assertThat(detalhe.lat()).isNotNull();
        assertThat(detalhe.lng()).isNotNull();
    }

    @Test
    void getMapTreeDetailDeveConterLatLngComoDoubleNuncaJts() {
        UUID id = criarArvore("Mangifera indica", LAT_DENTRO, LNG_DENTRO);

        MapTreeDetailDTO detalhe = mapService.getMapTreeDetail(id);

        assertThat(detalhe.lat()).isInstanceOf(Double.class);
        assertThat(detalhe.lng()).isInstanceOf(Double.class);
        assertThat(detalhe.lat()).isBetween(-90.0, 90.0);
        assertThat(detalhe.lng()).isBetween(-180.0, 180.0);
    }

    @Test
    void getMapTreeDetailCurrentRecordEhNullAteIntegracaoPessoa3() {
        UUID id = criarArvore("Ficus benjamina", LAT_DENTRO, LNG_DENTRO);

        MapTreeDetailDTO detalhe = mapService.getMapTreeDetail(id);

        assertThat(detalhe.currentRecord()).isNull();
    }

    // ----------------------------------------------------------------
    // GET /api/trees — listagem (listarTodas)
    // ----------------------------------------------------------------

    @Test
    void listarTodasDeveRetornarArvoresCriadas() {
        criarArvore("Ficus benjamina", LAT_DENTRO, LNG_DENTRO);
        criarArvore("Mangifera indica", LAT_DENTRO + 0.001, LNG_DENTRO + 0.001);

        var lista = arvoreService.listarTodas();

        assertThat(lista).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void listarTodasDeveRetornarLatLngComoDoubleNuncaJts() {
        criarArvore("Ficus benjamina", LAT_DENTRO, LNG_DENTRO);

        var lista = arvoreService.listarTodas();

        lista.forEach(item -> {
            if (item.lat() != null) {
                assertThat(item.lat()).isInstanceOf(Double.class);
                assertThat(item.lng()).isInstanceOf(Double.class);
            }
        });
    }

    @Test
    void listarTodasNaoDeveIncluirArvorasInativasPorPadrao() {
        UUID id = criarArvore("Inativa", LAT_DENTRO, LNG_DENTRO);
        arvoreService.deletar(id, gestor);

        var lista = arvoreService.listarTodas();

        boolean inativaPresente = lista.stream()
                .anyMatch(t -> "Inativa".equals(t.especie()));
        assertThat(inativaPresente).isFalse();
    }

    // ----------------------------------------------------------------
    // Helper privado
    // ----------------------------------------------------------------

    private UUID criarArvore(String especie, double lat, double lng) {
        ArvoreRequestDTO dto = new ArvoreRequestDTO(
                especie,
                especie.split(" ")[0],
                lat, lng,
                "Boa Viagem", "Av. Teste", null,
                5.0, 0.15, 3.0,
                EstadoGeral.BOM, Vigor.ALTO,
                Set.of(), Set.of(), Set.of(),
                EstruturaCopa.ASSIMETRICA,
                EstruturaTronco.SEM_DEFEITOS,
                EstruturaBase.NORMAL,
                InclinacaoTronco.AUSENTE,
                AncoragemRadicular.ESTAVEL,
                FluxoPedestre.BAIXO,
                FluxoAutomovel.BAIXO,
                TipoVia.RESIDENCIAL,
                Set.of(), Set.of(),
                new Conflito(), new Manejo(),
                null
        );
        ArvoreResponseDTO criada = arvoreService.salvar(dto, gestor);
        return criada.id();
    }
}