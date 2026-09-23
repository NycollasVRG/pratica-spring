package com.pratica.notificacao.domain.enums;

public enum SimNaoIgnorado {
    SIM(1),
    NAO(2),
    INDETERMINADO(3);

    private final Integer codigo;

    SimNaoIgnorado(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
