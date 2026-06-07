package com.example.arbor.dto.response;

import com.example.arbor.model.enums.Perfil;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        AuthUserResponseDTO usuario
) {
}
