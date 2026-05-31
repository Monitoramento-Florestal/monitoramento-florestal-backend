package com.example.arbor.dto.response;

import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nome,
        String email,
        Perfil perfilAcesso,
        Perfil role,
        Boolean ativo
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilAcesso(),
                usuario.getPerfilAcesso(),
                usuario.getAtivo());
    }
}
