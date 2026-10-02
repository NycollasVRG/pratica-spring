package com.pratica.notificacao.validation;

import com.pratica.notificacao.common.ClockConfig;

import jakarta.validation.Validator;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


final class ValidadorTestSupport {

    private ValidadorTestSupport() {}

    static Validator criarValidador() {
        AnnotationConfigApplicationContext contexto = new AnnotationConfigApplicationContext();
        contexto.register(ClockConfig.class);
        contexto.refresh();

        LocalValidatorFactoryBean fabrica = new LocalValidatorFactoryBean();
        fabrica.setApplicationContext(contexto);
        fabrica.afterPropertiesSet();
        return fabrica.getValidator();
    }
}
