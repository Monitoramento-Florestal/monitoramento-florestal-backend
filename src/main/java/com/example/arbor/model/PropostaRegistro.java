package com.example.arbor.model;

import com.example.arbor.model.enums.*;
import java.util.Set;

public record PropostaRegistro(
        Double alturaColetada,
        Double dapColetada,
        Double copaColetada,
        EstadoGeral estadoGeral,
        Vigor vigor,
        Set<Problema> problemasCopa,
        Set<Problema> problemasTronco,
        Set<Problema> problemasRaiz,
        EstruturaTronco estruturaTronco,
        EstruturaBase estruturaBase,
        EstruturaCopa estruturaCopa,
        InclinacaoTronco inclinacao,
        AncoragemRadicular ancoragem,
        FluxoPedestre fluxoPedestre,
        FluxoAutomovel fluxoAutomovel,
        TipoVia tipoVia,
        Set<AlvoPotencial> alvosPotenciais,
        Set<AlvoSensivel> alvosSensiveis,
        Conflito conflito,
        Manejo manejo,
        String observacoes
){}
