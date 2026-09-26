package com.pratica.notificacao.dto.response;

import com.pratica.notificacao.domain.enums.TipoNotificacao;
import java.time.LocalDate;

public record NotificacaoResponseDTO(
    String numeroNotificacao,
    TipoNotificacao tipoNotificacao,
    String agravoDoenca,
    LocalDate dataNotificacao,
    String ufNotificacao,
    String municipioNotificacao,
    String unidadeSaudeNotificadora,
    LocalDate dataPrimeirosSintomas,
    PacienteResponseDTO paciente,
    InvestigacaoResponseDTO investigacao
) {}
