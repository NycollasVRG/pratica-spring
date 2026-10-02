package com.pratica.notificacao.common;

public final class Textos {

    private Textos() {}

    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String aparado = texto.strip().replaceAll("\\s+", " ");
        return aparado.isEmpty() ? null : aparado;
    }
}
