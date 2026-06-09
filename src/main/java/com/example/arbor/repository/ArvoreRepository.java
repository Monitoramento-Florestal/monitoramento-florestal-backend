package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.enums.EstadoGeral;
import com.example.arbor.model.enums.Problema;
import com.example.arbor.model.enums.Vigor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT 'ARV-' || LPAD(nextval('seq_arvore_codigo')::TEXT, 5, '0')",
            nativeQuery = true)
    String gerarProximoCodigo();

    @Query(value = """
            SELECT a.* FROM tb_arvore a
            WHERE a.ativa = true
              AND a.localizacao IS NOT NULL
              AND ST_Within(
                    a.localizacao,
                    ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
                  )
              AND (:status IS NULL OR UPPER(a.estado_geral) = UPPER(CAST(:status AS VARCHAR)))
              AND (:species IS NULL OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:species AS VARCHAR), '%')))
              AND (:includeCut = true OR UPPER(a.estado_geral) != 'MORTA')
              AND (:search IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
            ORDER BY a.localizacao
            LIMIT :lim
            """,
            nativeQuery = true)
    List<Arvore> findWithinBbox(
            @Param("minLng")     double  minLng,
            @Param("minLat")     double  minLat,
            @Param("maxLng")     double  maxLng,
            @Param("maxLat")     double  maxLat,
            @Param("status")     String  status,
            @Param("species")    String  species,
            @Param("search")     String  search,
            @Param("includeCut") boolean includeCut,
            @Param("lim")        int     lim);

    @Query(value = """
            SELECT COUNT(*) FROM tb_arvore a
            WHERE a.ativa = true
              AND a.localizacao IS NOT NULL
              AND ST_Within(
                    a.localizacao,
                    ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
                  )
              AND (:status IS NULL OR UPPER(a.estado_geral) = UPPER(CAST(:status AS VARCHAR)))
              AND (:species IS NULL OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:species AS VARCHAR), '%')))
              AND (:includeCut = true OR UPPER(a.estado_geral) != 'MORTA')
              AND (:search IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
            """,
            nativeQuery = true)
    long countWithinBbox(
            @Param("minLng")     double  minLng,
            @Param("minLat")     double  minLat,
            @Param("maxLng")     double  maxLng,
            @Param("maxLat")     double  maxLat,
            @Param("status")     String  status,
            @Param("species")    String  species,
            @Param("search")     String  search,
            @Param("includeCut") boolean includeCut);

    @Query(value = """
            SELECT
                AVG(ST_Y(a.localizacao)) AS lat,
                AVG(ST_X(a.localizacao)) AS lng,
                COUNT(*)                 AS count
            FROM tb_arvore a
            WHERE a.ativa = true
              AND a.localizacao IS NOT NULL
              AND ST_Within(
                    a.localizacao,
                    ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
                  )
              AND (:status IS NULL OR UPPER(a.estado_geral) = UPPER(CAST(:status AS VARCHAR)))
              AND (:species IS NULL OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:species AS VARCHAR), '%')))
              AND (:includeCut = true OR UPPER(a.estado_geral) != 'MORTA')
              AND (:search IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
            GROUP BY
                FLOOR(ST_X(a.localizacao) / :gridSize),
                FLOOR(ST_Y(a.localizacao) / :gridSize)
            """,
            nativeQuery = true)
    List<Object[]> findClustersWithinBbox(
            @Param("minLng")     double  minLng,
            @Param("minLat")     double  minLat,
            @Param("maxLng")     double  maxLng,
            @Param("maxLat")     double  maxLat,
            @Param("status")     String  status,
            @Param("species")    String  species,
            @Param("search")     String  search,
            @Param("includeCut") boolean includeCut,
            @Param("gridSize")   double  gridSize);

    @Query(
            value = """
            SELECT * FROM public.tb_arvore a
            WHERE a.ativa = true
              AND (CAST(:search AS VARCHAR) IS NULL
                   OR LOWER(a.especie)                  LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, ''))     LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.bairro, ''))     LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
              AND (CAST(:especie AS VARCHAR) IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:especie AS VARCHAR), '%')))
              AND (CAST(:estadoGeral AS VARCHAR) IS NULL
                   OR UPPER(a.estado_geral) = UPPER(CAST(:estadoGeral AS VARCHAR)))
            ORDER BY a.codigo ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM public.tb_arvore a
            WHERE a.ativa = true
              AND (CAST(:search AS VARCHAR) IS NULL
                   OR LOWER(a.especie)                  LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, ''))     LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.bairro, ''))     LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
              AND (CAST(:especie AS VARCHAR) IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:especie AS VARCHAR), '%')))
              AND (CAST(:estadoGeral AS VARCHAR) IS NULL
                   OR UPPER(a.estado_geral) = UPPER(CAST(:estadoGeral AS VARCHAR)))
            """,
            nativeQuery = true
    )
    Page<Arvore> findAllAtivas(
            @Param("search")      String   search,
            @Param("especie")     String   especie,
            @Param("estadoGeral") String   estadoGeral,
            Pageable              pageable);

    @Query(
            value = """
            SELECT * FROM public.tb_arvore a
            WHERE (CAST(:search AS VARCHAR) IS NULL
                   OR LOWER(a.especie)                  LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, ''))     LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
              AND (CAST(:especie AS VARCHAR) IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:especie AS VARCHAR), '%')))
              AND (CAST(:estadoGeral AS VARCHAR) IS NULL
                   OR UPPER(a.estado_geral) = UPPER(CAST(:estadoGeral AS VARCHAR)))
            ORDER BY a.ativa DESC, a.codigo ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM public.tb_arvore a
            WHERE (CAST(:search AS VARCHAR) IS NULL
                   OR LOWER(a.especie)                  LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.nome_comum, '')) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))
                   OR LOWER(COALESCE(a.codigo, ''))     LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%')))
              AND (CAST(:especie AS VARCHAR) IS NULL
                   OR LOWER(a.especie) LIKE LOWER(CONCAT('%', CAST(:especie AS VARCHAR), '%')))
              AND (CAST(:estadoGeral AS VARCHAR) IS NULL
                   OR UPPER(a.estado_geral) = UPPER(CAST(:estadoGeral AS VARCHAR)))
            """,
            nativeQuery = true
    )
    Page<Arvore> findAllIncludingInativas(
            @Param("search")      String   search,
            @Param("especie")     String   especie,
            @Param("estadoGeral") String   estadoGeral,
            Pageable              pageable);
}



