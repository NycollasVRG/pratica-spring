package com.pratica.notificacao.dto.response;

import com.pratica.notificacao.domain.enums.ZonaResidencia;

public record EnderecoResponseDTO(
    String ufResidencia,
    String municipioResidencia,
    String distritoResidencia,
    String bairroResidencia,
    String logradouroResidencia,
    String numeroResidencia,
    String complementoResidencia,
    String geoCampo1,
    String geoCampo2,
    String pontoReferenciaResidencia,
    String cepResidencia,
    ZonaResidencia zonaResidencia,
    String paisResidencia
) {}
