package com.example.arbor.dto.response;

public record LoginResponseDTO(String accessToken, String refreshToken, String email, String nome) {
}
