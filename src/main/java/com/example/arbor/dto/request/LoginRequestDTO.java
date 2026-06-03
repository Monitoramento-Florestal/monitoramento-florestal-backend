package com.example.arbor.dto.request;

import com.example.arbor.dto.response.AuthUserResponseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String senha){}
