package com.example.arbor.dto.request;

import com.example.arbor.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioAdminRequestDTO(
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
        String nome,

        @Email
        @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres.")
        String email,

        Perfil perfilAcesso,

        Boolean ativo
) {
}
