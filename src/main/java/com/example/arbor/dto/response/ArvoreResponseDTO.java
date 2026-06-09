package com.example.arbor.dto.response;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.enums.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record ArvoreResponseDTO(
        UUID id,
        String codigo,
        String nomeComum,
        Double lat,
        Double lng,
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
        String observacoes,
        Object currentRecord
) {

    public ArvoreResponseDTO(Arvore arvore) {
        this(
                arvore.getId(),
                arvore.getCodigo(),
                arvore.getNomeComum(),
                arvore.toLat(),
                arvore.toLng(),
                arvore.getEspecie(),
                arvore.getBairro(),
                arvore.getRua(),
                arvore.getReferencia(),
                arvore.getAlturaAtual(),
                arvore.getDapAtual(),
                arvore.getCopaAtual(),
                arvore.getEstadoGeral(),
                arvore.getVigor(),
                copySet(arvore.getProblemasCopa()),
                copySet(arvore.getProblemasTronco()),
                copySet(arvore.getProblemasRaiz()),
                arvore.getEstruturaTronco(),
                arvore.getEstruturaBase(),
                arvore.getEstruturaCopa(),
                arvore.getInclinacao(),
                arvore.getAncoragem(),
                arvore.getFluxoPedestre(),
                arvore.getFluxoAutomovel(),
                arvore.getTipoVia(),
                copySet(arvore.getAlvosPotenciais()),
                copySet(arvore.getAlvosSensiveis()),
                copyConflito(arvore.getConflito()),
                copyManejo(arvore.getManejo()),
                arvore.getObservacoes(),
                null  // currentRecord — aguarda RegistroResumoDTO da Pessoa 3
        );
    }

    private static <T> Set<T> copySet(Set<T> source) {
        if (source == null) return null;
        return new LinkedHashSet<>(source);
    }

    private static Conflito copyConflito(Conflito source) {
        if (source == null) return null;
        return new Conflito(
                source.getFiacao(),
                source.getCalcada(),
                source.getIluminacao(),
                source.getEdificacao()
        );
    }

    private static Manejo copyManejo(Manejo source) {
        if (source == null) return null;
        return new Manejo(
                copySet(source.getAcoes()),
                source.getPrioridade()
        );
    }
}
