package com.pratica.notificacao.domain.enums;

public enum CriterioConfirmacao {
    LABORATORIAL(1),
    CLINICO_EPIDEMIOLOGICO(2);

    private final Integer codigo;

    CriterioConfirmacao(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
