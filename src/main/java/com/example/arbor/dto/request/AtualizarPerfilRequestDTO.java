package com.example.arbor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequestDTO(
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @Email
        @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
        String email
) {
}
