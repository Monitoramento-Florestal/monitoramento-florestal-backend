package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ArvoreRepository extends JpaRepository<Arvore, UUID> {

        // IMPLEMENTAR AS NOVAS FUNÇÕES DE FINDBY PARA AS 3/4 CONDIÇÕES
    //List<Arvore> findByCondicaoAtual(CondicaoArvore condicao);
    List<Arvore> findByAtivaTrue();

    Optional<Arvore> findByIdAndAtivaTrue(UUID id);

    //List<Arvore> findByCondicaoAtualAndAtivaTrue(CondicaoArvore condicao);

    List<Arvore> findByEspecieContainingIgnoreCaseAndAtivaTrue(String especie);

    List<Arvore> findByEstadoGeralAndAtivaTrue(EstadoGeral estadoGeral);

    List<Arvore> findByVigorAndAtivaTrue(Vigor vigor);

    List<Arvore> findByProblemasCopaContainingAndAtivaTrue(Problema problema);

    List<Arvore> findByProblemasTroncoContainingAndAtivaTrue(Problema problema);

    List<Arvore> findByProblemasRaizContainingAndAtivaTrue(Problema problema);







}
