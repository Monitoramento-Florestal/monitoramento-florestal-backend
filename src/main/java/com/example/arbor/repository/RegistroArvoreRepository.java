package com.example.arbor.repository;

import com.example.arbor.model.RegistroArvore;
import com.example.arbor.model.enums.StatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegistroArvoreRepository extends JpaRepository<RegistroArvore, UUID> {

    List<RegistroArvore> findByStatus(StatusRegistro status);

    List<RegistroArvore> findByPesquisadorId(UUID id);

    List<RegistroArvore> findByStatusAndPesquisadorId(StatusRegistro status, UUID id);

    List<RegistroArvore> findByArvoreId(UUID id);

}
