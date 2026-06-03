package com.example.arbor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequestDTO(
        @NotBlank String senhaAtual,

        @NotBlank
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                message = "A senha deve conter ao menos uma letra maiúscula e um número."
        )
        String novaSenha,

        @NotBlank String confirmarSenha
) {
}
