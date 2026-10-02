package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PeriodoGestacionalConverter implements AttributeConverter<PeriodoGestacional, Integer> {
    @Override
    public Integer convertToDatabaseColumn(PeriodoGestacional attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public PeriodoGestacional convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (PeriodoGestacional p : PeriodoGestacional.values()) {
            if (p.getCodigo().equals(dbData)) return p;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
