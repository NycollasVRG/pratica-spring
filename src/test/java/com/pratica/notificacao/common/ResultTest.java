package com.pratica.notificacao.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void okGuardaValor() {
        Result<String, String> resultado = Result.ok("abc");

        assertThat(resultado.isOk()).isTrue();
        if (!(resultado instanceof Result.Ok<?, ?> ok)) {
            throw new AssertionError("esperava Ok, mas veio: " + resultado);
        }
        assertThat(ok.value()).isEqualTo("abc");
    }

    @Test
    void errGuardaErro() {
        Result<String, String> resultado = Result.err("falhou");

        assertThat(resultado.isOk()).isFalse();
        if (!(resultado instanceof Result.Err<?, ?> erro)) {
            throw new AssertionError("esperava Err, mas veio: " + resultado);
        }
        assertThat(erro.error()).isEqualTo("falhou");
    }

    @Test
    void mapTransformaValorDeOk() {
        Result<String, String> resultado = Result.ok("abc");
        Result<Integer, String> mapeado = resultado.map(String::length);

        assertThat(mapeado.isOk()).isTrue();
        if (!(mapeado instanceof Result.Ok<?, ?> ok)) {
            throw new AssertionError("esperava Ok, mas veio: " + mapeado);
        }
        assertThat(ok.value()).isEqualTo(3);
    }

    @Test
    void mapPropagaErroSemTransformar() {
        Result<String, String> resultado = Result.err("deu ruim");
        Result<Integer, String> mapeado = resultado.map(String::length);

        assertThat(mapeado.isOk()).isFalse();
        if (!(mapeado instanceof Result.Err<?, ?> erro)) {
            throw new AssertionError("esperava Err, mas veio: " + mapeado);
        }
        assertThat(erro.error()).isEqualTo("deu ruim");
    }

    @Test
    void flatMapEncadeiaSucesso() {
        Result<String, String> resultado = Result.ok("abc");
        Result<Integer, String> encadeado = resultado.flatMap(v -> Result.ok(v.length()));

        assertThat(encadeado.isOk()).isTrue();
        if (!(encadeado instanceof Result.Ok<?, ?> ok)) {
            throw new AssertionError("esperava Ok, mas veio: " + encadeado);
        }
        assertThat(ok.value()).isEqualTo(3);
    }

    @Test
    void flatMapPropagaErroDoResultInterno() {
        Result<String, String> resultado = Result.ok("abc");
        Result<Integer, String> encadeado = resultado.flatMap(v -> Result.err("não deu"));

        assertThat(encadeado.isOk()).isFalse();
        if (!(encadeado instanceof Result.Err<?, ?> erro)) {
            throw new AssertionError("esperava Err, mas veio: " + encadeado);
        }
        assertThat(erro.error()).isEqualTo("não deu");
    }

    @Test
    void flatMapNaoExecutaQuandoJaFalhou() {
        Result<String, String> resultado = Result.err("primeiro erro");
        Result<Integer, String> encadeado = resultado.flatMap(v -> Result.ok(v.length()));

        assertThat(encadeado.isOk()).isFalse();
        if (!(encadeado instanceof Result.Err<?, ?> erro)) {
            throw new AssertionError("esperava Err, mas veio: " + encadeado);
        }
        assertThat(erro.error()).isEqualTo("primeiro erro");
    }
}
