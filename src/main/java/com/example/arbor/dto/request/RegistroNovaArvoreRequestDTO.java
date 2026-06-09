package com.example.arbor.dto.request;

import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.enums.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RegistroNovaArvoreRequestDTO(

        @NotBlank String especie,
        @NotBlank String bairro,
        @NotBlank String rua,
        String referencia,
        @DecimalMin(value = "-90.0", message = "Latitude deve ser >= -90.")
        @DecimalMax(value = "90.0", message = "Latitude deve ser <= 90.")
        Double lat,
        @DecimalMin(value = "-180.0", message = "Longitude deve ser >= -180.")
        @DecimalMax(value = "180.0", message = "Longitude deve ser <= 180.")
        Double lng,
        @NotNull Double alturaColetada,
        @NotNull Double dapColetada,
        @NotNull Double copaColetada,
        @NotNull EstadoGeral estadoGeral,
        @NotNull Vigor vigor,
        @NotNull Set<Problema> problemasCopa,
        @NotNull Set<Problema> problemasTronco,
        @NotNull Set<Problema> problemasRaiz,
        @NotNull EstruturaTronco estruturaTronco,
        @NotNull EstruturaBase estruturaBase,
        @NotNull EstruturaCopa estruturaCopa,
        @NotNull InclinacaoTronco inclinacaoTronco,
        @NotNull AncoragemRadicular ancoragem,
        @NotNull FluxoPedestre fluxoPedestre,
        @NotNull FluxoAutomovel fluxoAutomovel,
        @NotNull TipoVia tipoVia,
        @NotNull Set<AlvoPotencial> alvosPotenciais,
        @NotNull Set<AlvoSensivel> alvosSensiveis,
        @NotNull Conflito conflito,
        @NotNull Manejo manejo,
        String observacoes

) {}
