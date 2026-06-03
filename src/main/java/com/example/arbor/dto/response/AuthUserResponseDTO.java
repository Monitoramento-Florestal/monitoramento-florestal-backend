package com.example.arbor.dto.response;

import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;

import java.util.UUID;

public record AuthUserResponseDTO(UUID id, String nome, String email, Perfil perfilAcesso) {

    public static AuthUserResponseDTO from(Usuario usuario) {
        return new AuthUserResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilAcesso()
        );
    }

}
