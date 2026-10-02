package com.pratica.notificacao.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResidenciaValidator.class)
public @interface RegrasResidencia {

    String message() default "Dados de residência inválidos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
