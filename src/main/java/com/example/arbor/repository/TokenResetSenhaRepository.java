package com.example.arbor.repository;

import com.example.arbor.model.TokenResetSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenResetSenhaRepository extends JpaRepository<TokenResetSenha, UUID> {

    Optional<TokenResetSenha> findByTokenAndUsadoFalse(String token);
}