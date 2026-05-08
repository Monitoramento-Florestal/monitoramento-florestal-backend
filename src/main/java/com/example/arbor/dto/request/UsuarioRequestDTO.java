package com.example.arbor.dto.request;

import com.example.arbor.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(
                @NotBlank String nome,
                @NotBlank @Email String email,
                @NotBlank String senha,
                @NotNull Perfil perfilAcesso) {
}