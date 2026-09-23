package com.pratica.notificacao.domain.enums;

public enum EvolucaoCaso {
    CURA(1),
    OBITO_AGRAVO(2),
    OBITO_OUTRAS_CAUSAS(3),
    IGNORADO(9);

    private final Integer codigo;

    EvolucaoCaso(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
