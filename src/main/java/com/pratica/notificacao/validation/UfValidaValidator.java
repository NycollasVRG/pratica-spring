package com.pratica.notificacao.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class UfValidaValidator implements ConstraintValidator<UfValida, String> {

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA",
            "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN",
            "RO", "RR", "RS", "SC", "SE", "SP", "TO");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return UFS.contains(valor);
    }
}
