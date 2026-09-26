package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.TipoNotificacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record NotificacaoRequestDTO(
    @NotBlank(message = "O número da notificação é obrigatório")
    String numeroNotificacao,
    
    @NotNull(message = "O tipo da notificação é obrigatório")
    TipoNotificacao tipoNotificacao,
    
    @NotBlank(message = "O agravo/doença é obrigatório")
    String agravoDoenca,
    
    @NotNull(message = "A data da notificação não pode ser nula")
    @PastOrPresent(message = "A data da notificação não pode ser no futuro")
    LocalDate dataNotificacao,
    
    @NotBlank(message = "O UF da notificação é obrigatório")
    String ufNotificacao,
    
    @NotBlank(message = "O município da notificação é obrigatório")
    String municipioNotificacao,
    
    @NotBlank(message = "A unidade de saúde notificadora é obrigatória")
    String unidadeSaudeNotificadora,
    
    LocalDate dataPrimeirosSintomas,
    
    @Valid
    @NotNull(message = "Os dados do paciente são obrigatórios")
    PacienteRequestDTO paciente,
    
    @Valid
    InvestigacaoRequestDTO investigacao
) {}
