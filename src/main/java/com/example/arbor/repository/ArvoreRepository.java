package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArvoreRepository extends JpaRepository<Arvore, UUID> {

        // IMPLEMENTAR AS NOVAS FUNÇÕES DE FINDBY PARA AS 3/4 CONDIÇÕES
    //List<Arvore> findByCondicaoAtual(CondicaoArvore condicao);

    List<Arvore> findByEspecieContainingIgnoreCase(String especie);
}