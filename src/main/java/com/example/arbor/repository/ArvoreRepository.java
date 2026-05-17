package com.example.arbor.repository;

import com.example.arbor.model.Arvore;
import com.example.arbor.model.CondicaoArvore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArvoreRepository extends JpaRepository<Arvore, UUID> {

    List<Arvore> findByAtivaTrue();

    Optional<Arvore> findByIdAndAtivaTrue(UUID id);

    List<Arvore> findByCondicaoAtualAndAtivaTrue(CondicaoArvore condicao);

    List<Arvore> findByEspecieContainingIgnoreCaseAndAtivaTrue(String especie);
}
