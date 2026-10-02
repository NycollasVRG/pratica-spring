package com.pratica.notificacao.service;

import com.pratica.notificacao.common.Result;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.repository.NotificacaoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class NotificacaoServiceListagemTest {

    private NotificacaoRepository repositorio;
    private NotificacaoService servico;

    @BeforeEach
    void configurar() {
        repositorio = mock(NotificacaoRepository.class);
        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        servico = new NotificacaoService(repositorio, relogio);
    }

    @Test
    void paginaNegativaRetorna400SemConsultarBanco() {
        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar(null, null, null, null, null, null, false, -1, 10, "dataNotificacao", "desc");

        ErroServico.ParametroInvalido erro = parametroInvalido(resultado);
        assertThat(erro.violacoes().get(0).campo()).isEqualTo("pagina");
        nuncaConsultou();
    }

    @Test
    void tamanhoZeroRetorna400() {
        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar(null, null, null, null, null, null, false, 0, 0, "dataNotificacao", "desc");

        ErroServico.ParametroInvalido erro = parametroInvalido(resultado);
        assertThat(erro.violacoes().get(0).campo()).isEqualTo("tamanho");
        nuncaConsultou();
    }

    @Test
    void tamanhoAcimaDeCemRetorna400() {
        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar(null, null, null, null, null, null, false, 0, 101, "dataNotificacao", "desc");

        ErroServico.ParametroInvalido erro = parametroInvalido(resultado);
        assertThat(erro.violacoes().get(0).campo()).isEqualTo("tamanho");
        nuncaConsultou();
    }

    @Test
    void ordenarPorForaDaAllowlistRetorna400ComChavesAceitas() {
        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar(null, null, null, null, null, null, false, 0, 10, "senha", "desc");

        ErroServico.ParametroInvalido erro = parametroInvalido(resultado);
        assertThat(erro.violacoes().get(0).campo()).isEqualTo("ordenarPor");
        assertThat(erro.violacoes().get(0).mensagem()).contains("dataNotificacao");
        nuncaConsultou();
    }

    @Test
    void direcaoInvalidaRetorna400() {
        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar(null, null, null, null, null, null, false, 0, 10, "dataNotificacao", "ladoALado");

        ErroServico.ParametroInvalido erro = parametroInvalido(resultado);
        assertThat(erro.violacoes().get(0).campo()).isEqualTo("direcao");
        nuncaConsultou();
    }

    @Test
    void dataInicioDepoisDaDataFimRetorna400() {
        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar(null, null, null, null,
                        LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1),
                        false, 0, 10, "dataNotificacao", "desc");

        ErroServico.ParametroInvalido erro = parametroInvalido(resultado);
        assertThat(erro.violacoes().get(0).campo()).isEqualTo("dataFim");
        nuncaConsultou();
    }

    @Test
    void listagemValidaMontaEnvelopeEOrdenaPorAllowlist() {
        Notificacao primeira = new Notificacao();
        primeira.setNumeroNotificacao("1");
        Notificacao segunda = new Notificacao();
        segunda.setNumeroNotificacao("2");
        when(repositorio.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(primeira, segunda), PageRequest.of(0, 10), 25));

        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado =
                servico.listar("pb", "joão pessoa", null, null, null, null,
                        false, 0, 10, "nomePaciente", "DESC");

        assertThat(resultado.isOk()).isTrue();
        PaginaRespostaDTO<NotificacaoResponseDTO> envelope = envelopeDe(resultado);

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
        var ordem = solicitado.getSort().getOrderFor("paciente.nomePaciente");
        assertThat(ordem).isNotNull();
        assertThat(ordem.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void ordenacaoAscendenteERespeitada() {
        when(repositorio.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        servico.listar(null, null, null, null, null, null, false, 0, 10, "dataNascimento", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repositorio).findAll(any(Specification.class), captor.capture());
        var ordem = captor.getValue().getSort().getOrderFor("paciente.dataNascimento");
        assertThat(ordem).isNotNull();
        assertThat(ordem.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    // ------------------------------------------------------------------

    private void nuncaConsultou() {
        verify(repositorio, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    private static ErroServico.ParametroInvalido parametroInvalido(Result<?, ErroServico> resultado) {
        assertThat(resultado.isOk()).isFalse();
        ErroServico erro;
        if (resultado instanceof Result.Err<?, ?> err) {
            erro = (ErroServico) err.error();
        } else {
            erro = null;
        }
        assertThat(erro).isInstanceOf(ErroServico.ParametroInvalido.class);
        return (ErroServico.ParametroInvalido) erro;
    }

    @SuppressWarnings("unchecked")
    private static PaginaRespostaDTO<NotificacaoResponseDTO> envelopeDe(
            Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado) {
        if (!(resultado instanceof Result.Ok<?, ?> ok)) {
            throw new AssertionError("esperava Ok, veio: " + resultado);
        }
        return (PaginaRespostaDTO<NotificacaoResponseDTO>) ok.value();
    }
}
