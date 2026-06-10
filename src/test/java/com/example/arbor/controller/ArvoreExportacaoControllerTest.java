package com.example.arbor.controller;

import com.example.arbor.service.ArvoreService;
import com.example.arbor.service.ExportacaoArvoresService;
import com.example.arbor.service.ExportacaoArvoresService.ExportacaoPreparada;
import com.example.arbor.service.FormatoExportacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArvoreExportacaoControllerTest {

    @Mock
    private ArvoreService arvoreService;

    @Mock
    private ExportacaoArvoresService exportacaoService;

    @Test
    void exportarDeveRetornarHeadersDeDownloadEDelegarStreaming() throws Exception {
        LocalDate inicial = LocalDate.of(2026, 1, 1);
        LocalDate finalData = LocalDate.of(2026, 1, 31);
        ExportacaoPreparada preparada = new ExportacaoPreparada(
                inicial.atStartOfDay(),
                finalData.plusDays(1).atStartOfDay(),
                FormatoExportacao.CSV,
                "arvores_2026-01-01_a_2026-01-31.csv",
                3);
        when(exportacaoService.preparar(inicial, finalData, "csv")).thenReturn(preparada);
        ArvoreController controller = new ArvoreController(arvoreService, exportacaoService);

        ResponseEntity<byte[]> response =
                controller.exportar(inicial, finalData, "csv", null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("text/csv;charset=UTF-8");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains("arvores_2026-01-01_a_2026-01-31.csv");
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");

        byte[] corpo = response.getBody();
        assertThat(corpo).isNotNull();
        verify(exportacaoService).exportar(preparada, any(ByteArrayOutputStream.class));
    }
}
