package com.example.arbor.service;

import com.example.arbor.exception.ApiException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.projection.ArvoreExportacaoProjection;
import com.example.arbor.service.ExportacaoArvoresService.ExportacaoPreparada;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportacaoArvoresServiceTest {

    @Mock
    private ArvoreRepository repository;

    @Test
    void prepararDeveUsarInicioInclusivoEFimExclusivo() {
        ExportacaoArvoresService service = new ExportacaoArvoresService(repository, 100);
        LocalDate inicial = LocalDate.of(2026, 1, 1);
        LocalDate finalData = LocalDate.of(2026, 1, 31);
        when(repository.countParaExportacao(
                inicial.atStartOfDay(),
                finalData.plusDays(1).atStartOfDay()))
                .thenReturn(5L);

        ExportacaoPreparada resultado = service.preparar(inicial, finalData, "csv");

        assertThat(resultado.inicio()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(resultado.fimExclusivo()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(resultado.nomeArquivo()).isEqualTo("arvores_2026-01-01_a_2026-01-31.csv");
        assertThat(resultado.totalRegistros()).isEqualTo(5);
    }

    @Test
    void prepararDeveRejeitarIntervaloInvertido() {
        ExportacaoArvoresService service = new ExportacaoArvoresService(repository, 100);

        assertThatThrownBy(() -> service.preparar(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 31),
                "CSV"))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessageContaining("data inicial");
    }

    @Test
    void prepararDeveRejeitarFormatoInvalido() {
        ExportacaoArvoresService service = new ExportacaoArvoresService(repository, 100);

        assertThatThrownBy(() -> service.preparar(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "pdf"))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessageContaining("CSV ou XLSX");
    }

    @Test
    void prepararDeveRejeitarVolumeAcimaDoLimite() {
        ExportacaoArvoresService service = new ExportacaoArvoresService(repository, 10);
        LocalDate data = LocalDate.of(2026, 1, 1);
        when(repository.countParaExportacao(data.atStartOfDay(), data.plusDays(1).atStartOfDay()))
                .thenReturn(11L);

        assertThatThrownBy(() -> service.preparar(data, data, "xlsx"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(413));
    }

    @Test
    void exportarCsvDeveGerarUtf8ComCabecalhoEDadosSeguros() throws Exception {
        ExportacaoArvoresService service = new ExportacaoArvoresService(repository, 100);
        ExportacaoPreparada exportacao = exportacao(FormatoExportacao.CSV);
        ArvoreExportacaoProjection arvore = arvore("=SUM(1;1)");
        when(repository.findParaExportacao(
                exportacao.inicio(), exportacao.fimExclusivo(), 1_000, 0))
                .thenReturn(List.of(arvore));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.exportar(exportacao, output);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFF\"ID da arvore\";");
        assertThat(csv).contains("\"'=SUM(1;1)\"");
        assertThat(csv).contains("\"2026-01-15 10:30:00\"");
        assertThat(csv).contains("\"ATIVA\"");
    }

    @Test
    void exportarXlsxDeveGerarPlanilhaComTiposNumericosEData() throws Exception {
        ExportacaoArvoresService service = new ExportacaoArvoresService(repository, 100);
        ExportacaoPreparada exportacao = exportacao(FormatoExportacao.XLSX);
        ArvoreExportacaoProjection arvore = arvore("Mangifera indica");
        when(repository.findParaExportacao(
                exportacao.inicio(), exportacao.fimExclusivo(), 1_000, 0))
                .thenReturn(List.of(arvore));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.exportar(exportacao, output);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(output.toByteArray()))) {
            var sheet = workbook.getSheet("Arvores");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID da arvore");
            assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("Mangifera indica");
            assertThat(sheet.getRow(1).getCell(7).getNumericCellValue()).isEqualTo(-8.05);
            assertThat(sheet.getRow(1).getCell(9).getLocalDateTimeCellValue())
                    .isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 30));
        }
    }

    private ExportacaoPreparada exportacao(FormatoExportacao formato) {
        return new ExportacaoPreparada(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0),
                formato,
                "arvores_2026-01-01_a_2026-01-31." + formato.extensao(),
                1);
    }

    private ArvoreExportacaoProjection arvore(String especie) {
        ArvoreExportacaoProjection arvore = mock(ArvoreExportacaoProjection.class);
        when(arvore.getId()).thenReturn(UUID.fromString("b78d50d5-6ec7-42b4-9ec8-84c588e89415"));
        when(arvore.getCodigo()).thenReturn("ARV-00001");
        when(arvore.getEspecie()).thenReturn(especie);
        when(arvore.getNomeComum()).thenReturn("Mangueira");
        when(arvore.getBairro()).thenReturn("Dois Irmaos");
        when(arvore.getRua()).thenReturn("Rua Principal");
        when(arvore.getLatitude()).thenReturn(-8.05);
        when(arvore.getLongitude()).thenReturn(-34.88);
        when(arvore.getDataCadastro()).thenReturn(LocalDateTime.of(2026, 1, 15, 10, 30));
        when(arvore.getAtiva()).thenReturn(true);
        when(arvore.getAlturaAtual()).thenReturn(8.5);
        when(arvore.getDapAtual()).thenReturn(32.0);
        when(arvore.getCopaAtual()).thenReturn(5.2);
        when(arvore.getEstadoGeral()).thenReturn("BOM");
        when(arvore.getVigor()).thenReturn("ALTO");
        return arvore;
    }
}
