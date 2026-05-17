package com.example.arbor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarRecuperacaoDTO(
        @NotBlank @Email String email) {
}
