package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.ZonaResidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoRequestDTO(
    @Size(max = 2, message = "UF deve ter no máximo 2 caracteres")
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
    
    @NotBlank(message = "O país de residência não pode estar em branco")
    String paisResidencia
) {}
