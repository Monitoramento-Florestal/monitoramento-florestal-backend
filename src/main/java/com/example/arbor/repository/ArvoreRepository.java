package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArvoreRepository extends JpaRepository<Arvore, UUID> {

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByAtivaTrue();

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    Optional<Arvore> findByIdAndAtivaTrue(UUID id);

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByEspecieContainingIgnoreCaseAndAtivaTrue(String especie);

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByEstadoGeralAndAtivaTrue(EstadoGeral estadoGeral);

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByVigorAndAtivaTrue(Vigor vigor);

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByProblemasCopaContainingAndAtivaTrue(Problema problema);

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByProblemasTroncoContainingAndAtivaTrue(Problema problema);

    @EntityGraph(attributePaths = {
            "problemasCopa",
            "problemasTronco",
            "problemasRaiz",
            "alvosPotenciais",
            "alvosSensiveis",
            "manejo.acoes"
    })
    List<Arvore> findByProblemasRaizContainingAndAtivaTrue(Problema problema);
}
