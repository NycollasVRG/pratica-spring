package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import java.time.LocalDate;

public record NotificacaoFilterDTO(
    String uf,
    String municipio,
    TipoNotificacao tipo,
    Sexo sexo,
    LocalDate dataInicio,
    LocalDate dataFim,
    boolean duplicadas
) {}
