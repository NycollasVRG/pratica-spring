package com.pratica.notificacao.dto.response;

import com.pratica.notificacao.domain.enums.CriterioConfirmacao;
import com.pratica.notificacao.domain.enums.EvolucaoCaso;
import com.pratica.notificacao.domain.enums.SimNaoIgnorado;
import java.time.LocalDate;

public record InvestigacaoResponseDTO(
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
    LocalDate dataObito,
    LocalDate dataEncerramento
) {}
