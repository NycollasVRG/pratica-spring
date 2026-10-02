package com.pratica.notificacao.exception;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import tools.jackson.databind.exc.InvalidFormatException;


@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        ResponseEntity<Object> resposta = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (resposta == null) {
            return null;
        }

        Object corpo = resposta.getBody();
        if (corpo instanceof ProblemDetail problema) {
            if (problema.getType() == null) {
                problema.setType(Problemas.urn(statusCode.value()));
            }
            if (problema.getTitle() == null) {
                problema.setTitle(Problemas.titulo(statusCode.value()));
            }
            if (problema.getInstance() == null) {
                problema.setInstance(Problemas.uriAtual());
            }
            if (problema.getDetail() == null) {
                problema.setDetail(Problemas.titulo(statusCode.value()));
            }
        }

        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.addAll(resposta.getHeaders());
        cabecalhos.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(corpo, cabecalhos, resposta.getStatusCode());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail corpo = Problemas.problemDetail(status, "Método HTTP não suportado para este recurso", List.of());
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail corpo = Problemas.problemDetail(status,
                "Tipo de conteúdo não suportado; envie a requisição com Content-Type application/json", List.of());
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail corpo = Problemas.problemDetail(status,
                "Tipo de conteúdo aceito não está disponível; use Accept application/json", List.of());
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ViolacaoCampo> violacoes = List.of(
                new ViolacaoCampo(ex.getParameterName(), "Parâmetro obrigatório não informado"));
        ProblemDetail corpo = Problemas.problemDetail(status, "Requisição inválida", violacoes);
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ViolacaoCampo> violacoes = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                violacoes.add(new ViolacaoCampo(erro.getField(), erro.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(erro ->
                violacoes.add(new ViolacaoCampo(erro.getObjectName(), erro.getDefaultMessage())));

        ProblemDetail corpo = Problemas.problemDetail(status, "Requisição inválida", violacoes);
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String campo;
        if (ex instanceof MethodArgumentTypeMismatchException tipoEx) {
            campo = tipoEx.getName();
        } else {
            campo = ex.getPropertyName() != null ? ex.getPropertyName() : "";
        }

        Class<?> esperado = ex.getRequiredType();
        String mensagem = "Valor inválido";
        if (esperado != null && esperado.isEnum()) {
            mensagem = "Valor deve ser um de: " + constantesDeEnum(esperado);
        } else if (esperado == LocalDate.class) {
            mensagem = "Valor inválido; use o formato AAAA-MM-DD";
        } else if (esperado == Integer.class || esperado == Long.class
                || esperado == int.class || esperado == long.class) {
            mensagem = "Valor inválido; deve ser um número inteiro";
        } else if (esperado != null) {
            mensagem = "Valor inválido; tipo esperado: " + esperado.getSimpleName();
        }

        ProblemDetail corpo = Problemas.problemDetail(status, "Requisição inválida",
                List.of(new ViolacaoCampo(campo, mensagem)));
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail corpo = Problemas.problemDetail(status, detalheCorpoIlegivel(ex), List.of());
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail corpo = Problemas.problemDetail(status, "Recurso não encontrado", List.of());
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail corpo = Problemas.problemDetail(status, "Recurso não encontrado", List.of());
        return handleExceptionInternal(ex, corpo, headers, status, request);
    }

   
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> tratarViolacaoDeIntegridade(DataIntegrityViolationException ex, WebRequest request) {
        ProblemDetail corpo = Problemas.problemDetail(org.springframework.http.HttpStatus.CONFLICT,
                "Conflito: o recurso já existe ou está em uso por outra operação", List.of());
        return handleExceptionInternal(ex, corpo, new HttpHeaders(),
                org.springframework.http.HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> tratarBindException(BindException ex, WebRequest request) {
        List<ViolacaoCampo> violacoes = new ArrayList<>();
        ex.getFieldErrors().forEach(erro ->
                violacoes.add(new ViolacaoCampo(erro.getField(), erro.getDefaultMessage())));

        ProblemDetail corpo = Problemas.problemDetail(org.springframework.http.HttpStatus.BAD_REQUEST,
                "Requisição inválida", violacoes);
        return handleExceptionInternal(ex, corpo, new HttpHeaders(),
                org.springframework.http.HttpStatus.BAD_REQUEST, request);
    }

 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarErroInesperado(Exception ex, WebRequest request) {
        logger.error("Erro não tratado na requisição", ex);
        ProblemDetail corpo = Problemas.problemDetail(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado", List.of());
        return handleExceptionInternal(ex, corpo, new HttpHeaders(),
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    
    private static String detalheCorpoIlegivel(HttpMessageNotReadableException ex) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof InvalidFormatException formatoInvalido) {
                Class<?> alvo = formatoInvalido.getTargetType();
                Object valor = formatoInvalido.getValue();
                if (alvo != null && alvo.isEnum()) {
                    return "Valor \"" + valor + "\" inválido para " + alvo.getSimpleName()
                            + "; use um de: " + constantesDeEnum(alvo);
                }
                if (alvo == LocalDate.class) {
                    return "Data \"" + valor + "\" inválida; use o formato AAAA-MM-DD";
                }
                return "Valor inválido no corpo da requisição";
            }
            if (causa instanceof MismatchedInputException inesperado) {
                Class<?> alvo = inesperado.getTargetType();
                if (alvo == LocalDate.class) {
                    return "Datas no corpo da requisição devem usar o formato AAAA-MM-DD";
                }
                if (alvo != null) {
                    return "Valor inválido no corpo da requisição para " + alvo.getSimpleName();
                }
                return "Corpo da requisição inválido ou mal formatado";
            }
            causa = causa.getCause();
        }
        return "Corpo da requisição inválido ou mal formatado";
    }

    private static String constantesDeEnum(Class<?> enumClass) {
        StringBuilder nomes = new StringBuilder("[");
        for (Object constante : enumClass.getEnumConstants()) {
            if (nomes.length() > 1) {
                nomes.append(", ");
            }
            nomes.append(((Enum<?>) constante).name());
        }
        return nomes.append("]").toString();
    }
}
