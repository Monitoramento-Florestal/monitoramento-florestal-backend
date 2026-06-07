package com.example.arbor.model.enums;

public enum Perfil {
    ADMINISTRADOR,
    GESTOR,
    PESQUISADOR,
    PUBLICO_GERAL;

    public boolean isAdministrativo() {
        return this == ADMINISTRADOR || this == GESTOR;
    }
}
