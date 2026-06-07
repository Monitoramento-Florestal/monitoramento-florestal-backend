package com.example.arbor.dto.response;

import com.example.arbor.dto.resumo.UsuarioResumoDTO;
import com.example.arbor.model.Arvore;
import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record RegistroResponseDTO(
        UUID id,
        UsuarioResponseDTO pesquisador,
        LocalDateTime dataColeta,
        ArvoreResponseDTO arvore,
        UsuarioResponseDTO administradorResponsavel,
        LocalDateTime dataAnalise,
        String motivoRecusa,
        StatusRegistro status,
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
        InclinacaoTronco inclinacaoTronco,
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

                registro.getAdministradorResponsavel() == null
                        ? null
                        : new UsuarioResponseDTO(registro.getAdministradorResponsavel()),
                registro.getDataAnalise(),
                registro.getMotivoRecusa(),
                registro.getStatus(),

                registro.getAlturaColetada(),
                registro.getDapColetada(),
                registro.getCopaColetada(),

                registro.getEstadoGeral(),
                registro.getVigor(),
                copySet(registro.getProblemasCopa()),
                copySet(registro.getProblemasTronco()),
                copySet(registro.getProblemasRaiz()),

                registro.getEstruturaTronco(),
                registro.getEstruturaBase(),
                registro.getEstruturaCopa(),
                registro.getInclinacao(),
                registro.getAncoragem(),
                registro.getFluxoPedestre(),
                registro.getFluxoAutomovel(),
                registro.getTipoVia(),
                copySet(registro.getAlvosPotenciais()),
                copySet(registro.getAlvosSensiveis()),
                copyConflito(registro.getConflito()),
                copyManejo(registro.getManejo()),
                registro.getObservacoes()
        );
    }

    private static <T> Set<T> copySet(Set<T> source) {
        if (source == null) {
            return null;
        }

        return new LinkedHashSet<>(source);
    }

    private static Conflito copyConflito(Conflito source) {
        if (source == null) {
            return null;
        }

        return new Conflito(
                source.getFiacao(),
                source.getCalcada(),
                source.getIluminacao(),
                source.getEdificacao()
        );
    }

    private static Manejo copyManejo(Manejo source) {
        if (source == null) {
            return null;
        }

        return new Manejo(
                copySet(source.getAcoes()),
                source.getPrioridade()
        );
    }
}
