package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.RacaCor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RacaCorConverter implements AttributeConverter<RacaCor, Integer> {
    @Override
    public Integer convertToDatabaseColumn(RacaCor attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public RacaCor convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (RacaCor r : RacaCor.values()) {
            if (r.getCodigo().equals(dbData)) return r;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
