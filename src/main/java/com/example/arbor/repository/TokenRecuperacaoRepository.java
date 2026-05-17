package com.example.arbor.repository;

import com.example.arbor.model.TokenRecuperacao;
import com.example.arbor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, UUID> {

    Optional<TokenRecuperacao> findByCodigoAndUtilizadoFalse(String codigo);

    void deleteByUsuario(Usuario usuario);
}
