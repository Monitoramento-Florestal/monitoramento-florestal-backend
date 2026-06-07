package com.example.arbor.dto.response;

import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;

import java.util.UUID;

public record UsuarioPerfilResponseDTO(
        UUID id,
        String nome,
        String email,
        Perfil perfilAcesso,
        Boolean ativo
) {
    public static UsuarioPerfilResponseDTO from(Usuario usuario) {
        return new UsuarioPerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilAcesso(),
                usuario.getAtivo()
        );
    }
}
