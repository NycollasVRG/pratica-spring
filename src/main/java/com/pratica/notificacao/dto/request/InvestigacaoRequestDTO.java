package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.CriterioConfirmacao;
import com.pratica.notificacao.domain.enums.EvolucaoCaso;
import com.pratica.notificacao.domain.enums.SimNaoIgnorado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record InvestigacaoRequestDTO(
    @NotNull(message = "A data de investigação obrigatria")
    @PastOrPresent(message = "A data de investigação no pode ser no futuro")
    LocalDate dataInvestigacao,
    
    Integer classificacaoFinal,
    CriterioConfirmacao criterioConfirmacaoDescarte,
    SimNaoIgnorado autoctoneMunicipioResidencia,
    String ufLocalInfeccao,
    String paisLocalInfeccao,
    String municipioLocalInfeccao,
    String distritoLocalInfeccao,
    String bairroLocalInfeccao,
    SimNaoIgnorado doencaRelacionadaTrabalho,
    EvolucaoCaso evolucaoCaso,
    
    @PastOrPresent(message = "A data do óbito não pode ser no futuro")
    LocalDate dataObito,
    
    @PastOrPresent(message = "A data de encerramento não pode ser no futuro")
    LocalDate dataEncerramento
) {}
