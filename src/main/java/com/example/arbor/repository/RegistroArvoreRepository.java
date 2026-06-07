package com.example.arbor.repository;

import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.StatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface RegistroArvoreRepository extends JpaRepository<RegistroArvore, UUID> {

    List<RegistroArvore> findByStatus(StatusRegistro status);

    List<RegistroArvore> findByPesquisadorId(UUID id);

    List<RegistroArvore> findByStatusAndPesquisadorId(StatusRegistro status, UUID id);

    List<RegistroArvore> findByArvoreIdOrderByDataColetaDesc(UUID id);

    Optional<RegistroArvore> findTopByArvoreIdAndStatusOrderByVersaoDesc(
            UUID arvoreId,
            StatusRegistro status
    );

    List<RegistroArvore> findByArvoreIdOrderByVersaoDesc(UUID arvoreId);
}
