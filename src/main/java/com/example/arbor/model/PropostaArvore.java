package com.example.arbor.model;

public record PropostaArvore(
        String nomeComum,
        String especie,
        String bairro,
        String rua,
        String referencia,
        Double lat,
        Double lng
){}
