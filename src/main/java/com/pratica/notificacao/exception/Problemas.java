package com.pratica.notificacao.exception;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


public final class Problemas {

    private Problemas() {}

    public static ProblemDetail problemDetail(HttpStatusCode status, String detalhe, List<ViolacaoCampo> violacoes) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo(status.value()));
        problema.setType(urn(status.value()));
        problema.setInstance(uriAtual());
        if (violacoes != null && !violacoes.isEmpty()) {
            problema.setProperty("errors", violacoes);
        }
        return problema;
    }

    public static ResponseEntity<Object> resposta(ErroServico erro) {
        if (erro instanceof ErroServico.NaoEncontrado naoEncontrado) {
            return montar(org.springframework.http.HttpStatus.NOT_FOUND, naoEncontrado.detalhe(), List.of());
        }
        if (erro instanceof ErroServico.Conflito conflito) {
            return montar(org.springframework.http.HttpStatus.CONFLICT, conflito.detalhe(), List.of());
        }
        ErroServico.ParametroInvalido invalido = (ErroServico.ParametroInvalido) erro;
        return montar(org.springframework.http.HttpStatus.BAD_REQUEST, "Requisição inválida", invalido.violacoes());
    }

    public static URI urn(int status) {
        String codigo = switch (status) {
            case 400 -> "requisicao-invalida";
            case 404 -> "nao-encontrado";
            case 405 -> "metodo-nao-suportado";
            case 406 -> "conteudo-nao-aceito";
            case 409 -> "conflito";
            case 415 -> "conteudo-nao-suportado";
            case 500 -> "erro-interno";
            default -> "erro";
        };
        return URI.create("urn:pratica:notificacao:" + codigo);
    }

    public static String titulo(int status) {
        return switch (status) {
            case 400 -> "Requisição inválida";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método não suportado";
            case 406 -> "Tipo de conteúdo não aceito";
            case 409 -> "Conflito";
            case 415 -> "Tipo de conteúdo não suportado";
            case 500 -> "Erro interno do servidor";
            default -> "Erro na requisição";
        };
    }

    public static URI uriAtual() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
    }

    private static ResponseEntity<Object> montar(
            org.springframework.http.HttpStatus status, String detalhe, List<ViolacaoCampo> violacoes) {
        return ResponseEntity.status(status)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail(status, detalhe, violacoes));
    }
}
