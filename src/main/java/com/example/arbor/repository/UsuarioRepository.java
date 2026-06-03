package com.example.arbor.repository;

import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByAtivo(Boolean ativo);

    List<Usuario> findByPerfilAcesso(Perfil perfilAcesso);

    List<Usuario> findByPerfilAcessoAndAtivo(Perfil perfilAcesso, Boolean ativo);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
