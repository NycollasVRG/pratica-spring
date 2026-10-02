package com.pratica.notificacao.service;

import com.pratica.notificacao.common.Result;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.dto.request.EnderecoRequestDTO;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.request.PacienteRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.exception.ViolacaoCampo;
import com.pratica.notificacao.repository.NotificacaoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);

    private NotificacaoRepository repositorio;
    private NotificacaoService servico;

    @BeforeEach
    void configurar() {
        repositorio = mock(NotificacaoRepository.class);
        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        servico = new NotificacaoService(repositorio, relogio);
    }

    @Test
    void criarComNumeroJaExistenteRetornaConflitoSemSalvar() {
        when(repositorio.existsById("123")).thenReturn(true);

        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.criar(dto("123"));

        assertThat(resultado.isOk()).isFalse();
        ErroServico erro = erroDe(resultado);
        assertThat(erro).isInstanceOf(ErroServico.Conflito.class);
        assertThat(((ErroServico.Conflito) erro).detalhe()).contains("123");
        verify(repositorio, never()).save(any());
    }

    @Test
    void criarNovoNormalizaTextosEPaisBrasil() {
        when(repositorio.existsById("123")).thenReturn(false);

        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.criar(dto("123"));

        assertThat(resultado.isOk()).isTrue();
        Notificacao salva = salva();
        assertThat(salva.getAgravoDoenca()).isEqualTo("Febre Alta");
        assertThat(salva.getPaciente().getNomePaciente()).isEqualTo("Maria Silva");
        assertThat(salva.getPaciente().getNomeMae()).isEqualTo("Ana Maria");
        assertThat(salva.getPaciente().getEndereco().getPaisResidencia()).isEqualTo("Brasil");
    }

    @Test
    void criarParaFemininoMenorDeSeteCompletaNaoSeAplica() {
        when(repositorio.existsById("456")).thenReturn(false);

        servico.criar(dtoCriancinha("456"));

        Notificacao salva = salva();
        assertThat(salva.getPaciente().getGestante()).isEqualTo(PeriodoGestacional.NAO_SE_APLICA);
    }

    @Test
    void obterExistenteRetornaOkComONumero() {
        Notificacao entidade = new Notificacao();
        entidade.setNumeroNotificacao("123");
        when(repositorio.findById("123")).thenReturn(Optional.of(entidade));

        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.obter("123");

        assertThat(resultado.isOk()).isTrue();
        if (!(resultado instanceof Result.Ok<?, ?> ok)) {
            throw new AssertionError("esperava Ok, mas veio: " + resultado);
        }
        assertThat(((NotificacaoResponseDTO) ok.value()).numeroNotificacao()).isEqualTo("123");
    }

    @Test
    void obterInexistenteRetornaNaoEncontrado() {
        when(repositorio.findById("999")).thenReturn(Optional.empty());

        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.obter("999");

        assertThat(resultado.isOk()).isFalse();
        assertThat(erroDe(resultado)).isInstanceOf(ErroServico.NaoEncontrado.class);
    }

    @Test
    void atualizarComNumeroDivergenteRetornaParametroInvalidoSemPersistir() {
        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.atualizar("123", dto("456"));

        assertThat(resultado.isOk()).isFalse();
        ErroServico erro = erroDe(resultado);
        assertThat(erro).isInstanceOf(ErroServico.ParametroInvalido.class);
        ViolacaoCampo violacao = ((ErroServico.ParametroInvalido) erro).violacoes().get(0);
        assertThat(violacao.campo()).isEqualTo("numeroNotificacao");
        verify(repositorio, never()).existsById(any());
        verify(repositorio, never()).save(any());
    }

    @Test
    void atualizarInexistenteRetornaNaoEncontradoSemSalvar() {
        when(repositorio.existsById("123")).thenReturn(false);

        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.atualizar("123", dto("123"));

        assertThat(resultado.isOk()).isFalse();
        assertThat(erroDe(resultado)).isInstanceOf(ErroServico.NaoEncontrado.class);
        verify(repositorio, never()).save(any());
    }

    @Test
    void atualizarExistenteSalva() {
        when(repositorio.existsById("123")).thenReturn(true);

        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.atualizar("123", dto("123"));

        assertThat(resultado.isOk()).isTrue();
        verify(repositorio).save(any(Notificacao.class));
    }

    @Test
    void removerInexistenteRetornaNaoEncontrado() {
        when(repositorio.existsById("999")).thenReturn(false);

        Result<Void, ErroServico> resultado = servico.remover("999");

        assertThat(resultado.isOk()).isFalse();
        assertThat(erroDe(resultado)).isInstanceOf(ErroServico.NaoEncontrado.class);
        verify(repositorio, never()).deleteById(any());
    }

    @Test
    void removerExistenteRetornaOkEDeleta() {
        when(repositorio.existsById("123")).thenReturn(true);

        Result<Void, ErroServico> resultado = servico.remover("123");

        assertThat(resultado.isOk()).isTrue();
        verify(repositorio).deleteById("123");
    }

    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static ErroServico erroDe(Result<?, ErroServico> resultado) {
        if (resultado instanceof Result.Err) {
            return ((Result.Err<?, ErroServico>) resultado).error();
        }
        fail("esperava Err, mas veio: " + resultado);
        return null;
    }

    private Notificacao salva() {
        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(repositorio).save(captor.capture());
        return captor.getValue();
    }

    private static NotificacaoRequestDTO dto(String numero) {
        return new NotificacaoRequestDTO(
                numero,
                TipoNotificacao.INDIVIDUAL,
                "  Febre   Alta ",
                LocalDate.of(2026, 1, 10),
                "PB",
                "João Pessoa",
                "UBS Centro",
                LocalDate.of(2026, 1, 5),
                pacienteComIdade(),
                null);
    }

    private static NotificacaoRequestDTO dtoCriancinha(String numero) {
        return new NotificacaoRequestDTO(
                numero,
                TipoNotificacao.INDIVIDUAL,
                "Febre Alta",
                LocalDate.of(2026, 1, 10),
                "PB",
                "João Pessoa",
                "UBS Centro",
                LocalDate.of(2026, 1, 5),
                pacienteCriancinha(),
                null);
    }

    private static PacienteRequestDTO pacienteComIdade() {
        return new PacienteRequestDTO(
                "  Maria   Silva ",
                LocalDate.of(1990, 5, 20),
                null,
                Sexo.FEMININO,
                null,
                null, null, null,
                "  Ana   Maria ",
                null,
                endereco());
    }

    private static PacienteRequestDTO pacienteCriancinha() {
        return new PacienteRequestDTO(
                "Joana Silva",
                HOJE.minusYears(5),
                null,
                Sexo.FEMININO,
                null,
                null, null, null,
                "Ana Maria",
                null,
                endereco());
    }

    private static EnderecoRequestDTO endereco() {
        return new EnderecoRequestDTO(
                "PB", "João Pessoa", null, null, null, null, null,
                null, null, null, "58000000", null, " ");
    }
}
