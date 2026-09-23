package com.pratica.notificacao.domain.enums;

public enum TipoNotificacao {
    NEGATIVA(1),
    INDIVIDUAL(2),
    SURTO(3),
    TRACOMA(4);

    private final Integer codigo;

    TipoNotificacao(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
