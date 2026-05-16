package com.example.arbor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetarSenhaRequestDTO(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String novaSenha) {
}
