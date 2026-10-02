package com.pratica.notificacao.dto.response;

import java.util.List;

/**
 * Envelope de listagem paginada: {conteudo, pagina, tamanho, totalElementos, totalPaginas}.
 */
public record PaginaRespostaDTO<T>(
    List<T> conteudo,
    int pagina,
    int tamanho,
    long totalElementos,
    int totalPaginas
) {}
