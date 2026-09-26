package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.CriterioConfirmacao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CriterioConfirmacaoConverter implements AttributeConverter<CriterioConfirmacao, Integer> {
    @Override
    public Integer convertToDatabaseColumn(CriterioConfirmacao attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public CriterioConfirmacao convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (CriterioConfirmacao c : CriterioConfirmacao.values()) {
            if (c.getCodigo().equals(dbData)) return c;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
