package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.Sexo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SexoConverter implements AttributeConverter<Sexo, Character> {
    @Override
    public Character convertToDatabaseColumn(Sexo attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public Sexo convertToEntityAttribute(Character dbData) {
        if (dbData == null) return null;
        for (Sexo s : Sexo.values()) {
            if (s.getCodigo().equals(dbData)) return s;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
