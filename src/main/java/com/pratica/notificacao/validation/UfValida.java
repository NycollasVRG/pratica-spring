package com.pratica.notificacao.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UfValidaValidator.class)
public @interface UfValida {

    String message() default "UF inválida; use uma das 27 siglas em maiúsculas (ex: PB)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
