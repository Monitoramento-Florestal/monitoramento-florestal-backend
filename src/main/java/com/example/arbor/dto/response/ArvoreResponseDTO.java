package com.example.arbor.dto.response;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.enums.*;

import java.util.Set;
import java.util.UUID;

public record ArvoreResponseDTO(
        UUID id,
        String especie,
        String bairro,
        String rua,
        String referencia,
        Double alturaAtual,
        Double dapAtual,
        Double copaAtual,
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
) {

    public ArvoreResponseDTO(Arvore arvore) {

        this(
                arvore.getId(),
                arvore.getEspecie(),
                arvore.getBairro(),
                arvore.getRua(),
                arvore.getReferencia(),
                arvore.getAlturaAtual(),
                arvore.getDapAtual(),
                arvore.getCopaAtual(),
                arvore.getEstadoGeral(),
                arvore.getVigor(),
                arvore.getProblemasCopa(),
                arvore.getProblemasTronco(),
                arvore.getProblemasRaiz(),
                arvore.getEstruturaTronco(),
                arvore.getEstruturaBase(),
                arvore.getEstruturaCopa(),
                arvore.getInclinacao(),
                arvore.getAncoragem(),
                arvore.getFluxoPedestre(),
                arvore.getFluxoAutomovel(),
                arvore.getTipoVia(),
                arvore.getAlvosPotenciais(),
                arvore.getAlvosSensiveis(),
                arvore.getConflito(),
                arvore.getManejo(),
                arvore.getObservacoes()
        );
    }
}