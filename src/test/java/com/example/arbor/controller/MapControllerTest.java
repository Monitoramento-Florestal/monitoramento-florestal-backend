package com.example.arbor.controller;

import com.example.arbor.dto.map.MapClusterDTO;
import com.example.arbor.dto.map.MapTreeDTO;
import com.example.arbor.dto.map.MapTreeDetailDTO;
import com.example.arbor.dto.map.MapTreesResponseDTO;
import com.example.arbor.exception.RecursoNaoEncontradoException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Vigor;
import com.example.arbor.service.MapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapControllerTest {

    @Mock
    private MapService mapService;

    @InjectMocks
    private MapController controller;

    // ----------------------------------------------------------------
    // GET /api/map/trees — modo trees
    // ----------------------------------------------------------------

    @Test
    void getMapTreesDeveRetornar200ComModeTreesDoService() {
        MapTreesResponseDTO resposta = MapTreesResponseDTO.ofTrees(3L, List.of(
                mapTreeDTO(-8.05, -34.88),
                mapTreeDTO(-8.06, -34.87),
                mapTreeDTO(-8.07, -34.86)
        ));
        when(mapService.getMapTrees(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(resposta);

        ResponseEntity<MapTreesResponseDTO> resultado = controller.getMapTrees(
                -34.92, -8.10, -34.85, -8.02,
                15, null, null, null, false, null);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody().mode()).isEqualTo("trees");
        assertThat(resultado.getBody().totalInView()).isEqualTo(3L);
        assertThat(resultado.getBody().items()).hasSize(3);
        assertThat(resultado.getBody().clusters()).isEmpty();
    }

    @Test
    void getMapTreesDeveRetornar200ComModeClusterDoService() {
        MapTreesResponseDTO resposta = MapTreesResponseDTO.ofClusters(300L, List.of(
                new MapClusterDTO(-8.05, -34.88, 150L),
                new MapClusterDTO(-8.10, -34.90, 150L)
        ));
        when(mapService.getMapTrees(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(resposta);

        ResponseEntity<MapTreesResponseDTO> resultado = controller.getMapTrees(
                -34.92, -8.10, -34.85, -8.02,
                10, null, null, null, false, null);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody().mode()).isEqualTo("cluster");
        assertThat(resultado.getBody().clusters()).hasSize(2);
        assertThat(resultado.getBody().items()).isEmpty();
    }

    @Test
    void getMapTreesDeveDelegarTodosOsFiltrosAoService() {
        when(mapService.getMapTrees(
                eq(-34.92), eq(-8.10), eq(-34.85), eq(-8.02),
                eq(16), eq("BOM"), eq("ficus"), eq("Ficus"), eq(true), eq(100)))
                .thenReturn(MapTreesResponseDTO.ofTrees(0L, List.of()));

        controller.getMapTrees(-34.92, -8.10, -34.85, -8.02,
                16, "BOM", "ficus", "Ficus", true, 100);

        verify(mapService).getMapTrees(
                eq(-34.92), eq(-8.10), eq(-34.85), eq(-8.02),
                eq(16), eq("BOM"), eq("ficus"), eq("Ficus"), eq(true), eq(100));
    }

    @Test
    void getMapTreesDevePassarNullParaParametrosOpcionaisNaoInformados() {
        when(mapService.getMapTrees(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                isNull(), isNull(), isNull(), isNull(), eq(false), isNull()))
                .thenReturn(MapTreesResponseDTO.ofTrees(0L, List.of()));

        controller.getMapTrees(-34.92, -8.10, -34.85, -8.02,
                null, null, null, null, false, null);

        verify(mapService).getMapTrees(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                isNull(), isNull(), isNull(), isNull(), eq(false), isNull());
    }

    @Test
    void getMapTreesDevePropagarRequisicaoInvalidaExceptionDoBbox() {
        doThrow(new RequisicaoInvalidaException("minLng deve ser menor que maxLng."))
                .when(mapService).getMapTrees(
                        anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                        any(), any(), any(), any(), anyBoolean(), any());

        assertThatThrownBy(() -> controller.getMapTrees(
                -34.85, -8.10, -34.92, -8.02,
                15, null, null, null, false, null))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessageContaining("minLng");
    }

    @Test
    void getMapTreesRespostaJsonNaoDeveConterObjetoJtsNemGeoPoint() {
        MapTreeDTO dto = mapTreeDTO(-8.05, -34.88);
        when(mapService.getMapTrees(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(MapTreesResponseDTO.ofTrees(1L, List.of(dto)));

        ResponseEntity<MapTreesResponseDTO> resultado = controller.getMapTrees(
                -34.92, -8.10, -34.85, -8.02,
                15, null, null, null, false, null);

        MapTreeDTO item = resultado.getBody().items().get(0);
        assertThat(item.lat()).isInstanceOf(Double.class);
        assertThat(item.lng()).isInstanceOf(Double.class);
        assertThat(item.lat()).isEqualTo(-8.05);
        assertThat(item.lng()).isEqualTo(-34.88);
    }

    // ----------------------------------------------------------------
    // GET /api/map/trees/{treeId}/detail
    // ----------------------------------------------------------------

    @Test
    void getMapTreeDetailDeveRetornar200ComDTODoService() {
        UUID id = UUID.randomUUID();
        MapTreeDetailDTO detalhe = mapTreeDetailDTO(id);
        when(mapService.getMapTreeDetail(id)).thenReturn(detalhe);

        ResponseEntity<MapTreeDetailDTO> resultado = controller.getMapTreeDetail(id);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isEqualTo(detalhe);
        verify(mapService).getMapTreeDetail(id);
    }

    @Test
    void getMapTreeDetailDevePropagar404DoService() {
        UUID id = UUID.randomUUID();
        when(mapService.getMapTreeDetail(id))
                .thenThrow(new RecursoNaoEncontradoException("Árvore não encontrada: " + id));

        assertThatThrownBy(() -> controller.getMapTreeDetail(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getMapTreeDetailRespostaDeveConterLatLngComoDoubleENaoJts() {
        UUID id = UUID.randomUUID();
        MapTreeDetailDTO detalhe = mapTreeDetailDTO(id);
        when(mapService.getMapTreeDetail(id)).thenReturn(detalhe);

        ResponseEntity<MapTreeDetailDTO> resultado = controller.getMapTreeDetail(id);

        assertThat(resultado.getBody().lat()).isInstanceOf(Double.class);
        assertThat(resultado.getBody().lng()).isInstanceOf(Double.class);
    }

    @Test
    void getMapTreeDetailCurrentRecordEhNullAteIntegracaoPessoa3() {
        UUID id = UUID.randomUUID();
        when(mapService.getMapTreeDetail(id)).thenReturn(mapTreeDetailDTO(id));

        ResponseEntity<MapTreeDetailDTO> resultado = controller.getMapTreeDetail(id);

        assertThat(resultado.getBody().currentRecord()).isNull();
    }

    @Test
    void getMapTreeDetailDeveDelegarIdAoService() {
        UUID id = UUID.randomUUID();
        when(mapService.getMapTreeDetail(id)).thenReturn(mapTreeDetailDTO(id));

        controller.getMapTreeDetail(id);

        verify(mapService).getMapTreeDetail(eq(id));
    }

    // ----------------------------------------------------------------
    // Builders de fixture
    // ----------------------------------------------------------------

    private MapTreeDTO mapTreeDTO(double lat, double lng) {
        return new MapTreeDTO(
                UUID.randomUUID(),
                "ARV-00001",
                "Mangueira",
                "Mangifera indica",
                lat,
                lng,
                EstadoGeral.BOM,
                null  // ultimaMedicao — aguarda Pessoa 3
        );
    }

    private MapTreeDetailDTO mapTreeDetailDTO(UUID id) {
        return new MapTreeDetailDTO(
                id,
                "ARV-00001",
                "Mangueira",
                "Mangifera indica",
                -8.05,
                -34.88,
                "Boa Viagem",
                "Av. Boa Viagem",
                null,
                EstadoGeral.BOM,
                Vigor.ALTO,
                null,
                null   // currentRecord — aguarda Pessoa 3
        );
    }
}
