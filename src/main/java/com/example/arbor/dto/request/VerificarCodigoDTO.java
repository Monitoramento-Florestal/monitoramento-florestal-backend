package com.example.arbor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record VerificarCodigoDTO(
        @NotBlank @Size(min = 6, max = 6) String codigo
        @NotBlank @Email String email
) {}

