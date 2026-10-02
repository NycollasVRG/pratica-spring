package com.pratica.notificacao.validation;

import java.text.Normalizer;

import com.pratica.notificacao.dto.request.EnderecoRequestDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ResidenciaValidator implements ConstraintValidator<RegrasResidencia, EnderecoRequestDTO> {

    @Override
    public boolean isValid(EnderecoRequestDTO endereco, ConstraintValidatorContext contexto) {
        if (endereco == null) {
            return true;
        }
        contexto.disableDefaultConstraintViolation();

        boolean valido = true;
        boolean resideNoBrasil = resideNoBrasil(endereco.paisResidencia());
        boolean semUf = endereco.ufResidencia() == null || endereco.ufResidencia().isBlank();
        boolean semMunicipio = endereco.municipioResidencia() == null || endereco.municipioResidencia().isBlank();

        if (resideNoBrasil && semUf) {
            contexto.buildConstraintViolationWithTemplate(
                    "A UF de residência é obrigatória para pacientes que residem no Brasil")
                    .addPropertyNode("ufResidencia")
                    .addConstraintViolation();
            valido = false;
        }
        if (!semUf && semMunicipio) {
            contexto.buildConstraintViolationWithTemplate(
                    "O município de residência é obrigatório quando a UF é informada")
                    .addPropertyNode("municipioResidencia")
                    .addConstraintViolation();
            valido = false;
        }
        return valido;
    }

    private static boolean resideNoBrasil(String pais) {
        if (pais == null || pais.isBlank()) {
            return true;
        }
        return normalizar(pais).equals("brasil");
    }

    private static String normalizar(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcentos.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
