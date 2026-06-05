package com.example.arbor.dto.response;

import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record RegistroResponseDTO(
        UUID id,
        UsuarioResponseDTO pesquisador,
        LocalDateTime dataColeta,
        ArvoreResponseDTO arvore,
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
) {
    public RegistroResponseDTO(RegistroArvore registro){
        this(
                registro.getId(),
                new UsuarioResponseDTO(registro.getPesquisador()),
                registro.getDataColeta(),
                registro.getArvore() == null
                        ? null
                        : new ArvoreResponseDTO(registro.getArvore()),

                registro.getAlturaColetada(),
                registro.getDapColetada(),
                registro.getCopaColetada(),

                registro.getEstadoGeral(),
                registro.getVigor(),
                registro.getProblemasCopa(),
                registro.getProblemasTronco(),
                registro.getProblemasRaiz(),

                registro.getEstruturaTronco(),
                registro.getEstruturaBase(),
                registro.getEstruturaCopa(),
                registro.getInclinacao(),
                registro.getAncoragem(),
                registro.getFluxoPedestre(),
                registro.getFluxoAutomovel(),
                registro.getTipoVia(),
                registro.getAlvosPotenciais(),
                registro.getAlvosSensiveis(),
                registro.getConflito(),
                registro.getManejo(),
                registro.getObservacoes()
        );
    }
}
