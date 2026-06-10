package com.example.arbor.repository;

import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.StatusRegistro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroArvoreRepository extends JpaRepository<RegistroArvore, UUID> {

    @EntityGraph(attributePaths = {
            "pesquisador",
            "administradorResponsavel",
            "arvore",
            "arvore.problemasCopa",
            "arvore.problemasTronco",
            "arvore.problemasRaiz",
            "arvore.alvosPotenciais",
            "arvore.alvosSensiveis",
            "arvore.manejo.acoes",
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<RegistroArvore> findByStatus(StatusRegistro status);

    @EntityGraph(attributePaths = {
            "pesquisador",
            "administradorResponsavel",
            "arvore",
            "arvore.problemasCopa",
            "arvore.problemasTronco",
            "arvore.problemasRaiz",
            "arvore.alvosPotenciais",
            "arvore.alvosSensiveis",
            "arvore.manejo.acoes",
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<RegistroArvore> findByPesquisadorId(UUID id);

    @EntityGraph(attributePaths = {
            "pesquisador",
            "administradorResponsavel",
            "arvore",
            "arvore.problemasCopa",
            "arvore.problemasTronco",
            "arvore.problemasRaiz",
            "arvore.alvosPotenciais",
            "arvore.alvosSensiveis",
            "arvore.manejo.acoes",
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    Long countByPesquisadorId(UUID id);

    List<RegistroArvore> findByStatusAndPesquisadorId(StatusRegistro status, UUID id);

    @EntityGraph(attributePaths = {
            "pesquisador",
            "administradorResponsavel",
            "arvore",
            "arvore.problemasCopa",
            "arvore.problemasTronco",
            "arvore.problemasRaiz",
            "arvore.alvosPotenciais",
            "arvore.alvosSensiveis",
            "arvore.manejo.acoes",
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<RegistroArvore> findByArvoreIdOrderByDataColetaDesc(UUID id);

    Optional<RegistroArvore> findTopByArvoreIdOrderByVersaoDesc(UUID arvoreId);

    Optional<RegistroArvore> findTopByArvoreIdAndStatusOrderByVersaoDesc(
            UUID arvoreId,
            StatusRegistro status
    );
}
