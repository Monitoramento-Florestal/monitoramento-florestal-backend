package com.example.arbor.dto;

import com.example.arbor.model.Usuario;

public record RecusarRegistroRequestDTO (Usuario administrador, String motivoRecusa) {}
