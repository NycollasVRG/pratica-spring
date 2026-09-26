package com.pratica.notificacao.domain.enums.converters;

import com.pratica.notificacao.domain.enums.TipoNotificacao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoNotificacaoConverter implements AttributeConverter<TipoNotificacao, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TipoNotificacao attribute) {
        if (attribute == null) return null;
        return attribute.getCodigo();
    }
    @Override
    public TipoNotificacao convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        for (TipoNotificacao t : TipoNotificacao.values()) {
            if (t.getCodigo().equals(dbData)) return t;
        }
        throw new IllegalArgumentException("Unknown dbData: " + dbData);
    }
}
