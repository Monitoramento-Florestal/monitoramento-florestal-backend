package com.example.arbor.dto.response;

import com.example.arbor.model.Conflito;
import com.example.arbor.model.Manejo;
import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.*;

import java.time.LocalDateTime;
import java.util.Set;

public record RegistroNovaArvoreResponseDTO(
        String especie,
        String bairro,
        String rua,
        String referencia,
        Double lat,
        Double lng,
        UsuarioResponseDTO pesquisador,
        LocalDateTime dataColeta,
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
    public RegistroNovaArvoreResponseDTO(RegistroArvore registro) {
        this(
                registro.getEspecie(),
                registro.getBairro(),
                registro.getRua(),
                registro.getReferencia(),
                registro.toLatNova(),
                registro.toLngNova(),

                new UsuarioResponseDTO(registro.getPesquisador()),
                registro.getDataColeta(),

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
