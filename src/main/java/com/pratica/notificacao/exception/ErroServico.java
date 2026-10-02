package com.pratica.notificacao.exception;

import java.util.List;

public abstract class ErroServico extends RuntimeException {

    public ErroServico(String message) {
        super(message);
    }

    public static class NaoEncontrado extends ErroServico {
        public NaoEncontrado(String detalhe) {
            super(detalhe);
        }
    }

    public static class Conflito extends ErroServico {
        public Conflito(String detalhe) {
            super(detalhe);
        }
    }

    public static class ParametroInvalido extends ErroServico {
        private final List<ViolacaoCampo> violacoes;

        public ParametroInvalido(List<ViolacaoCampo> violacoes) {
            super("Parâmetro Inválido");
            this.violacoes = List.copyOf(violacoes);
        }

        public List<ViolacaoCampo> getViolacoes() {
            return violacoes;
        }
    }
}
