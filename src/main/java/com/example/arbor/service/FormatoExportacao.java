package com.example.arbor.service;

import com.example.arbor.exception.RequisicaoInvalidaException;

import java.util.Locale;

public enum FormatoExportacao {
    CSV("text/csv;charset=UTF-8", "csv"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String mediaType;
    private final String extensao;

    FormatoExportacao(String mediaType, String extensao) {
        this.mediaType = mediaType;
        this.extensao = extensao;
    }

    public String mediaType() {
        return mediaType;
    }

    public String extensao() {
        return extensao;
    }

    public static FormatoExportacao parse(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RequisicaoInvalidaException("O formato da exportacao deve ser CSV ou XLSX.");
        }

        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RequisicaoInvalidaException("Formato de exportacao invalido. Use CSV ou XLSX.");
        }
    }
}
