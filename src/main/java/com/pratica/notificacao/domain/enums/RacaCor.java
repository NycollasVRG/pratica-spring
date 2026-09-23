package com.pratica.notificacao.domain.enums;

public enum RacaCor {
    BRANCA(1),
    PRETA(2),
    AMARELA(3),
    PARDA(4),
    INDIGENA(5);

    private final Integer codigo;

    RacaCor(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
