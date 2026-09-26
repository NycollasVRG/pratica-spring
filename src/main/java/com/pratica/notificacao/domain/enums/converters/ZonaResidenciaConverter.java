package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.ZonaResidencia;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ZonaResidenciaConverter implements AttributeConverter<ZonaResidencia, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ZonaResidencia attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public ZonaResidencia convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (ZonaResidencia z : ZonaResidencia.values()) {
            if (z.getCodigo().equals(dbData)) return z;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
