package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.SimNaoIgnorado;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SimNaoIgnoradoConverter implements AttributeConverter<SimNaoIgnorado, Integer> {
    @Override
    public Integer convertToDatabaseColumn(SimNaoIgnorado attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public SimNaoIgnorado convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (SimNaoIgnorado s : SimNaoIgnorado.values()) {
            if (s.getCodigo().equals(dbData)) return s;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
