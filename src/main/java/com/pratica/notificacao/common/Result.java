package com.pratica.notificacao.common;

import java.util.function.Function;


public sealed interface Result<T, E> permits Result.Ok, Result.Err {

    record Ok<T, E>(T value) implements Result<T, E> {}

    record Err<T, E>(E error) implements Result<T, E> {}

    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    default boolean isOk() {
        return this instanceof Result.Ok;
    }

    @SuppressWarnings("unchecked")
    default <U> Result<U, E> map(Function<T, U> mapper) {
        if (this instanceof Result.Ok) {
            T valor = ((Result.Ok<T, E>) this).value();
            return new Result.Ok<>(mapper.apply(valor));
        }
        E erro = ((Result.Err<T, E>) this).error();
        return new Result.Err<>(erro);
    }

    @SuppressWarnings("unchecked")
    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        if (this instanceof Result.Ok) {
            T valor = ((Result.Ok<T, E>) this).value();
            return mapper.apply(valor);
        }
        E erro = ((Result.Err<T, E>) this).error();
        return new Result.Err<>(erro);
    }
}
