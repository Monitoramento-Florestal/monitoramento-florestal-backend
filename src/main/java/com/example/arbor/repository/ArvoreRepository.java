package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArvoreRepository extends JpaRepository<Arvore, UUID> {

    List<Arvore> findByAtivaTrue();

    Long countByAtivaFalse();

    Optional<Arvore> findByIdAndAtivaTrue(UUID id);

    List<Arvore> findByEspecieContainingIgnoreCaseAndAtivaTrue(String especie);

    List<Arvore> findByEstadoGeralAndAtivaTrue(EstadoGeral estadoGeral);

    List<Arvore> findByVigorAndAtivaTrue(Vigor vigor);

    List<Arvore> findByProblemasCopaContainingAndAtivaTrue(Problema problema);

    List<Arvore> findByProblemasTroncoContainingAndAtivaTrue(Problema problema);

    List<Arvore> findByProblemasRaizContainingAndAtivaTrue(Problema problema);

    @Query("""
        SELECT COUNT(DISTINCT a)
        FROM Arvore a
        LEFT JOIN a.problemasCopa pc
        LEFT JOIN a.problemasTronco pt
        LEFT JOIN a.problemasRaiz pr
        WHERE pc <> com.example.arbor.model.enums.Problema.NENHUM
           OR pt <> com.example.arbor.model.enums.Problema.NENHUM
           OR pr <> com.example.arbor.model.enums.Problema.NENHUM
    """)
    Long countArvoresInjuriadas();

}
