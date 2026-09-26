package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.EvolucaoCaso;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EvolucaoCasoConverter implements AttributeConverter<EvolucaoCaso, Integer> {
    @Override
    public Integer convertToDatabaseColumn(EvolucaoCaso attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public EvolucaoCaso convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (EvolucaoCaso e : EvolucaoCaso.values()) {
            if (e.getCodigo().equals(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
