package com.example.arbor.service;

import com.example.arbor.exception.ApiException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.repository.ArvoreRepository;
import com.example.arbor.repository.projection.ArvoreExportacaoProjection;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportacaoArvoresService {

    private static final int TAMANHO_LOTE = 1_000;
    private static final int LIMITE_LINHAS_XLSX = 1_048_575;
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUNAS = {
            "ID da arvore", "Codigo", "Especie", "Nome comum", "Bairro", "Rua", "Referencia",
            "Latitude", "Longitude", "Data de cadastro", "Status", "Altura (m)", "DAP (cm)",
            "Copa (m)", "Estado geral", "Vigor", "Observacoes"
    };

    private final ArvoreRepository repository;
    private final long limiteRegistros;

    public ExportacaoArvoresService(
            ArvoreRepository repository,
            @Value("${arbor.exportacao.max-registros:100000}") long limiteRegistros) {
        this.repository = repository;
        this.limiteRegistros = limiteRegistros;
    }

    @Transactional(readOnly = true)
    public ExportacaoPreparada preparar(LocalDate dataInicial, LocalDate dataFinal, String formatoInformado) {
        if (dataInicial == null || dataFinal == null) {
            throw new RequisicaoInvalidaException("Data inicial e data final sao obrigatorias.");
        }
        if (dataInicial.isAfter(dataFinal)) {
            throw new RequisicaoInvalidaException("A data inicial nao pode ser posterior a data final.");
        }

        FormatoExportacao formato = FormatoExportacao.parse(formatoInformado);
        LocalDateTime inicio = dataInicial.atStartOfDay();
        LocalDateTime fimExclusivo = dataFinal.plusDays(1).atStartOfDay();
        long total = repository.countParaExportacao(inicio, fimExclusivo);
        long limiteFormato = formato == FormatoExportacao.XLSX
                ? Math.min(limiteRegistros, LIMITE_LINHAS_XLSX)
                : limiteRegistros;

        if (total > limiteFormato) {
            throw new ApiException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "EXPORT_TOO_LARGE",
                    "A exportacao possui " + total + " registros. Reduza o intervalo para no maximo "
                            + limiteFormato + " registros.");
        }

        String nomeArquivo = "arvores_" + dataInicial + "_a_" + dataFinal + "." + formato.extensao();
        return new ExportacaoPreparada(
                inicio, fimExclusivo, formato, nomeArquivo, total);
    }

    @Transactional(readOnly = true)
    public void exportar(ExportacaoPreparada exportacao, OutputStream outputStream) throws IOException {
        if (exportacao.formato() == FormatoExportacao.CSV) {
            exportarCsv(exportacao, outputStream);
        } else {
            exportarXlsx(exportacao, outputStream);
        }
    }

    private void exportarCsv(ExportacaoPreparada exportacao, OutputStream outputStream) throws IOException {
        outputStream.write(0xEF);
        outputStream.write(0xBB);
        outputStream.write(0xBF);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        escreverLinhaCsv(writer, COLUNAS);

        percorrerLotes(exportacao, lote -> {
            for (ArvoreExportacaoProjection arvore : lote) {
                escreverLinhaCsv(writer, valores(arvore));
            }
        });
        writer.flush();
    }

    private void exportarXlsx(ExportacaoPreparada exportacao, OutputStream outputStream) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);

        try {
            Sheet sheet = workbook.createSheet("Arvores");
            CellStyle cabecalho = criarEstiloCabecalho(workbook);
            CellStyle dataHora = criarEstiloDataHora(workbook);
            int[] indiceLinha = {0};

            Row header = sheet.createRow(indiceLinha[0]++);
            for (int coluna = 0; coluna < COLUNAS.length; coluna++) {
                Cell cell = header.createCell(coluna);
                cell.setCellValue(COLUNAS[coluna]);
                cell.setCellStyle(cabecalho);
            }
            sheet.createFreezePane(0, 1);

            percorrerLotes(exportacao, lote -> {
                for (ArvoreExportacaoProjection arvore : lote) {
                    Row row = sheet.createRow(indiceLinha[0]++);
                    preencherLinhaXlsx(row, arvore, dataHora);
                }
            });

            int[] larguras = {38, 16, 28, 24, 20, 28, 28, 14, 14, 22, 12, 14, 14, 14, 18, 14, 48};
            for (int coluna = 0; coluna < larguras.length; coluna++) {
                sheet.setColumnWidth(coluna, larguras[coluna] * 256);
            }

            workbook.write(outputStream);
            outputStream.flush();
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    private void percorrerLotes(ExportacaoPreparada exportacao, ConsumidorLote consumidor) throws IOException {
        long deslocamento = 0;
        while (deslocamento < exportacao.totalRegistros()) {
            List<ArvoreExportacaoProjection> lote = repository.findParaExportacao(
                    exportacao.inicio(), exportacao.fimExclusivo(), TAMANHO_LOTE, deslocamento);
            if (lote.isEmpty()) {
                break;
            }
            consumidor.aceitar(lote);
            deslocamento += lote.size();
        }
    }

    private String[] valores(ArvoreExportacaoProjection arvore) {
        return new String[]{
                textoSeguro(arvore.getId()),
                textoSeguro(arvore.getCodigo()),
                textoSeguro(arvore.getEspecie()),
                textoSeguro(arvore.getNomeComum()),
                textoSeguro(arvore.getBairro()),
                textoSeguro(arvore.getRua()),
                textoSeguro(arvore.getReferencia()),
                numero(arvore.getLatitude()),
                numero(arvore.getLongitude()),
                arvore.getDataCadastro() == null ? "" : DATA_HORA.format(arvore.getDataCadastro()),
                Boolean.TRUE.equals(arvore.getAtiva()) ? "ATIVA" : "INATIVA",
                numero(arvore.getAlturaAtual()),
                numero(arvore.getDapAtual()),
                numero(arvore.getCopaAtual()),
                textoSeguro(arvore.getEstadoGeral()),
                textoSeguro(arvore.getVigor()),
                textoSeguro(arvore.getObservacoes())
        };
    }

    private void escreverLinhaCsv(BufferedWriter writer, String[] valores) throws IOException {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                writer.write(';');
            }
            writer.write('"');
            writer.write(valores[i] == null ? "" : valores[i].replace("\"", "\"\""));
            writer.write('"');
        }
        writer.newLine();
    }

    private void preencherLinhaXlsx(
            Row row,
            ArvoreExportacaoProjection arvore,
            CellStyle estiloDataHora) {
        setTexto(row, 0, arvore.getId());
        setTexto(row, 1, arvore.getCodigo());
        setTexto(row, 2, arvore.getEspecie());
        setTexto(row, 3, arvore.getNomeComum());
        setTexto(row, 4, arvore.getBairro());
        setTexto(row, 5, arvore.getRua());
        setTexto(row, 6, arvore.getReferencia());
        setNumero(row, 7, arvore.getLatitude());
        setNumero(row, 8, arvore.getLongitude());
        if (arvore.getDataCadastro() != null) {
            Cell cell = row.createCell(9);
            cell.setCellValue(arvore.getDataCadastro());
            cell.setCellStyle(estiloDataHora);
        }
        setTexto(row, 10, Boolean.TRUE.equals(arvore.getAtiva()) ? "ATIVA" : "INATIVA");
        setNumero(row, 11, arvore.getAlturaAtual());
        setNumero(row, 12, arvore.getDapAtual());
        setNumero(row, 13, arvore.getCopaAtual());
        setTexto(row, 14, arvore.getEstadoGeral());
        setTexto(row, 15, arvore.getVigor());
        setTexto(row, 16, arvore.getObservacoes());
    }

    private CellStyle criarEstiloCabecalho(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle criarEstiloDataHora(SXSSFWorkbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    private void setTexto(Row row, int coluna, Object valor) {
        if (valor != null) {
            row.createCell(coluna).setCellValue(valor.toString());
        }
    }

    private void setNumero(Row row, int coluna, Double valor) {
        if (valor != null) {
            row.createCell(coluna).setCellValue(valor);
        }
    }

    private String textoSeguro(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.toString();
        if (!texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
            return "'" + texto;
        }
        return texto;
    }

    private String numero(Double valor) {
        return valor == null ? "" : Double.toString(valor);
    }

    @FunctionalInterface
    private interface ConsumidorLote {
        void aceitar(List<ArvoreExportacaoProjection> lote) throws IOException;
    }

    public record ExportacaoPreparada(
            LocalDateTime inicio,
            LocalDateTime fimExclusivo,
            FormatoExportacao formato,
            String nomeArquivo,
            long totalRegistros) {
    }
}
