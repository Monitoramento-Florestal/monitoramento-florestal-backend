package com.example.arbor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
        @NotBlank @Size(min = 6, max = 6) String codigo,
        @NotBlank @Email String email,

        @NotBlank
        @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                message = "A senha deve conter ao menos uma letra maiuscula e um numero."
        )
        String novaSenha,

        @NotBlank String confirmarSenha
) {
}
