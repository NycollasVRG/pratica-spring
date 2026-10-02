package com.pratica.notificacao.validation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.ObjectProvider;

import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.dto.request.PacienteRequestDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PacienteValidator implements ConstraintValidator<RegrasPaciente, PacienteRequestDTO> {

    private final ObjectProvider<Clock> relogio;

    public PacienteValidator(ObjectProvider<Clock> relogio) {
        this.relogio = relogio;
    }

    @Override
    public boolean isValid(PacienteRequestDTO paciente, ConstraintValidatorContext contexto) {
        if (paciente == null) {
            return true;
        }
        contexto.disableDefaultConstraintViolation();

        boolean valido = true;

        boolean idadeAusente = paciente.idade() == null && paciente.dataNascimento() == null;
        if (idadeAusente) {
            contexto.buildConstraintViolationWithTemplate(
                    "A idade é obrigatória quando a data de nascimento não é informada")
                    .addPropertyNode("idade")
                    .addConstraintViolation();
            valido = false;
        }

        Integer idade = idadeEfetiva(paciente);
        boolean menorDeSete = idade != null && idade < 7;
        Sexo sexo = paciente.sexo();
        PeriodoGestacional gestante = paciente.gestante();

        if (sexo != null) {
            if (sexo == Sexo.FEMININO && gestante == null && !menorDeSete) {
                contexto.buildConstraintViolationWithTemplate(
                        "O período gestacional é obrigatório para pacientes do sexo feminino")
                        .addPropertyNode("gestante")
                        .addConstraintViolation();
                valido = false;
            }
            if (menorDeSete && gestante != null && gestante != PeriodoGestacional.NAO_SE_APLICA) {
                contexto.buildConstraintViolationWithTemplate(
                        "Para menores de 7 anos o período gestacional deve ser Não se aplica")
                        .addPropertyNode("gestante")
                        .addConstraintViolation();
                valido = false;
            }
            if (sexo != Sexo.FEMININO && gestante != null && gestante != PeriodoGestacional.NAO_SE_APLICA) {
                contexto.buildConstraintViolationWithTemplate(
                        "O período gestacional só se aplica a pacientes do sexo feminino")
                        .addPropertyNode("gestante")
                        .addConstraintViolation();
                valido = false;
            }
        }

        return valido;
    }

    private Integer idadeEfetiva(PacienteRequestDTO paciente) {
        if (paciente.dataNascimento() != null) {
            LocalDate hoje = LocalDate.now(relogio.getIfAvailable(Clock::systemDefaultZone));
            if (paciente.dataNascimento().isAfter(hoje)) {
                return null;
            }
            return Period.between(paciente.dataNascimento(), hoje).getYears();
        }
        return paciente.idade();
    }
}
