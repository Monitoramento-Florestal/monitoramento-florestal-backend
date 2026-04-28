package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.CondicaoArvore;
import com.example.arbor.model.StatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArvoreRepository extends JpaRepository<Arvore, UUID> {

    List<Arvore> findByCondicao(CondicaoArvore condicao);

    //novas buscas
    List<Arvore> findByStatus(StatusRegistro status);

    List<Arvore> findByPesquisadorId(UUID id);

    List<Arvore> findByStatusAndPesquisadorId(StatusRegistro status, UUID id);
    //ate aqui

    List<Arvore> findByEspecieContainingIgnoreCase(String especie);
}