package com.pratica.notificacao.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificacaoMapper {

    Notificacao paraEntidade(NotificacaoRequestDTO dto);

    NotificacaoResponseDTO paraResposta(Notificacao entidade);

    void atualizarEntidade(NotificacaoRequestDTO dto, @MappingTarget Notificacao entidade);
}
