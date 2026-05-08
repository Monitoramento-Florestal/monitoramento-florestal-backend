package com.example.arbor.dto.response;

import com.example.arbor.model.Perfil;
import com.example.arbor.model.Usuario;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nome,
        String email,
        Perfil perfilAcesso
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfilAcesso());
    }
}