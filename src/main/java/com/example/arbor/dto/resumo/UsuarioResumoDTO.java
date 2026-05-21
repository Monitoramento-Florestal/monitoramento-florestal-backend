package com.example.arbor.dto.resumo;

import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;

import java.util.UUID;

public record UsuarioResumoDTO(
        UUID id,
        String nome,
        String email,
        Perfil perfilAcesso
) {
    public UsuarioResumoDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfilAcesso());
    }
}

