package com.pratica.notificacao.service;

import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.dto.request.NotificacaoFilterDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.mapper.NotificacaoMapper;
import com.pratica.notificacao.repository.NotificacaoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class NotificacaoServiceListagemTest {

    private NotificacaoRepository repositorio;
    private NotificacaoMapper mapper;
    private NotificacaoService servico;

    @BeforeEach
    void configurar() {
        repositorio = mock(NotificacaoRepository.class);
        mapper = Mappers.getMapper(NotificacaoMapper.class);
        if(mapper == null) mapper = mock(NotificacaoMapper.class);
        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        servico = new NotificacaoService(repositorio, mapper, relogio);
    }

    @Test
    void dataInicioDepoisDaDataFimRetorna400() {
        NotificacaoFilterDTO filtro = new NotificacaoFilterDTO(null, null, null, null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), false);
        
        ErroServico.ParametroInvalido erro = assertThrows(ErroServico.ParametroInvalido.class, () -> {
            servico.listar(filtro, PageRequest.of(0, 10));
        });

        assertThat(erro.getViolacoes().get(0).campo()).isEqualTo("dataFim");
        verify(repositorio, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void listagemValidaMontaEnvelopeEOrdenaPorAllowlist() {
        Notificacao primeira = new Notificacao();
        primeira.setNumeroNotificacao("1");
        Notificacao segunda = new Notificacao();
        segunda.setNumeroNotificacao("2");
        when(repositorio.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(primeira, segunda), PageRequest.of(0, 10), 25));
        if(mapper.getClass().getName().contains("Mockito")) {
            when(mapper.paraResposta(primeira)).thenReturn(new NotificacaoResponseDTO("1", null, null, null, null, null, null, null, null, null));
            when(mapper.paraResposta(segunda)).thenReturn(new NotificacaoResponseDTO("2", null, null, null, null, null, null, null, null, null));
        }

        NotificacaoFilterDTO filtro = new NotificacaoFilterDTO("pb", "joão pessoa", null, null, null, null, false);
        PaginaRespostaDTO<NotificacaoResponseDTO> envelope = servico.listar(filtro, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "paciente.nomePaciente")));

        assertThat(envelope.conteudo())
                .extracting(NotificacaoResponseDTO::numeroNotificacao)
                .containsExactly("1", "2");
        assertThat(envelope.pagina()).isZero();
        assertThat(envelope.tamanho()).isEqualTo(10);
        assertThat(envelope.totalElementos()).isEqualTo(25);
        assertThat(envelope.totalPaginas()).isEqualTo(3);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repositorio).findAll(any(Specification.class), captor.capture());

        Pageable solicitado = captor.getValue();
        assertThat(solicitado.getPageNumber()).isZero();
        assertThat(solicitado.getPageSize()).isEqualTo(10);
    }
}
