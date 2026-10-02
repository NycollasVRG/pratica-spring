package com.pratica.notificacao.domain.enums;


public enum PeriodoGestacional {
    PRIMEIRO_TRIMESTRE(1),
    SEGUNDO_TRIMESTRE(2),
    TERCEIRO_TRIMESTRE(3),
    IDADE_GESTACIONAL_IGNORADA(4),
    NAO(5),
    NAO_SE_APLICA(6),
    IGNORADO(9);

    private final Integer codigo;

    PeriodoGestacional(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
