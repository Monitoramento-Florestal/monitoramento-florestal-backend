package com.example.arbor.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ArvoreExportacaoProjection {

    UUID getId();

    String getCodigo();

    String getEspecie();

    String getNomeComum();

    String getBairro();

    String getRua();

    String getReferencia();

    Double getLatitude();

    Double getLongitude();

    LocalDateTime getDataCadastro();

    Boolean getAtiva();

    Double getAlturaAtual();

    Double getDapAtual();

    Double getCopaAtual();

    String getEstadoGeral();

    String getVigor();

    String getObservacoes();
}
