package com.pratica.notificacao.domain.enums;

public enum Sexo {
    MASCULINO('M'),
    FEMININO('F'),
    IGNORADO('I');

    private final Character codigo;

    Sexo(Character codigo) {
        this.codigo = codigo;
    }

    public Character getCodigo() {
        return codigo;
    }
}
