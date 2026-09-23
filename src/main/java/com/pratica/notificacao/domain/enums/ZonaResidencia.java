package com.pratica.notificacao.domain.enums;

public enum ZonaResidencia {
    URBANA(1),
    RURAL(2),
    PERIURBANA(3),
    IGNORADO(9);

    private final Integer codigo;

    ZonaResidencia(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
