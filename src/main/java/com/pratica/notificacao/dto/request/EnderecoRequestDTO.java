package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.ZonaResidencia;
import com.pratica.notificacao.validation.RegrasResidencia;
import com.pratica.notificacao.validation.UfValida;
import jakarta.validation.constraints.Pattern;

@RegrasResidencia
public record EnderecoRequestDTO(
    @UfValida
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

    @Pattern(regexp = "^(\\d{5}-?\\d{3})?$", message = "O CEP deve ter 8 dígitos (com ou sem hífen)")
    String cepResidencia,
    ZonaResidencia zonaResidencia,
    
    String paisResidencia
) {}
