package com.pratica.notificacao.exception;

import java.util.List;


public sealed interface ErroServico
        permits ErroServico.NaoEncontrado, ErroServico.Conflito, ErroServico.ParametroInvalido {

    record NaoEncontrado(String detalhe) implements ErroServico {}

    record Conflito(String detalhe) implements ErroServico {}

    record ParametroInvalido(List<ViolacaoCampo> violacoes) implements ErroServico {
        public ParametroInvalido {
            violacoes = List.copyOf(violacoes);
        }
    }
}
