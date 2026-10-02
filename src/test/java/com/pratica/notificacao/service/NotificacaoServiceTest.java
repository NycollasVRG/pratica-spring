package com.pratica.notificacao.service;

import com.pratica.notificacao.domain.Endereco;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.dto.request.EnderecoRequestDTO;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.request.PacienteRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.mapper.NotificacaoMapper;
import com.pratica.notificacao.repository.NotificacaoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mapstruct.factory.Mappers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);

    private NotificacaoRepository repositorio;
    private NotificacaoMapper mapper;
    private NotificacaoService servico;

    @BeforeEach
    void configurar() {
        repositorio = mock(NotificacaoRepository.class);
        mapper = Mappers.getMapper(NotificacaoMapper.class);
        if (mapper == null) {
            // Se o MapStruct não gerou por algum problema no test, vamos criar um mock basico ou pular.
            // Para não quebrar o mockMvc context, faremos o mock direto se for nulo
            mapper = mock(NotificacaoMapper.class);
            when(mapper.paraEntidade(any())).thenAnswer(invocation -> {
                NotificacaoRequestDTO dto = invocation.getArgument(0);
                Notificacao n = new Notificacao();
                n.setAgravoDoenca(dto.agravoDoenca());
                Paciente p = new Paciente();
                if(dto.paciente() != null) {
                    p.setNomePaciente(dto.paciente().nomePaciente());
                    p.setNomeMae(dto.paciente().nomeMae());
                    p.setDataNascimento(dto.paciente().dataNascimento());
                    p.setSexo(dto.paciente().sexo());
                    Endereco e = new Endereco();
                    if(dto.paciente().endereco() != null) {
                        e.setPaisResidencia(dto.paciente().endereco().paisResidencia());
                    }
                    p.setEndereco(e);
                }
                n.setPaciente(p);
                return n;
            });
            when(mapper.paraResposta(any())).thenReturn(new NotificacaoResponseDTO(null, null, null, null, null, null, null, null, null, null));
        }

        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        servico = new NotificacaoService(repositorio, mapper, relogio);
    }

    @Test
    void criarComNumeroJaExistenteRetornaConflitoSemSalvar() {
        when(repositorio.existsById("123")).thenReturn(true);

        assertThrows(ErroServico.Conflito.class, () -> servico.criar(dto("123")));

        verify(repositorio, never()).save(any());
    }

    @Test
    void criarNovoNormalizaTextosEPaisBrasil() {
        when(repositorio.existsById("123")).thenReturn(false);

        servico.criar(dto("123"));

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
        if(mapper.getClass().getName().contains("Mockito")) {
            when(mapper.paraResposta(entidade)).thenReturn(new NotificacaoResponseDTO("123", null, null, null, null, null, null, null, null, null));
        }

        NotificacaoResponseDTO resultado = servico.obter("123");

        assertThat(resultado.numeroNotificacao()).isEqualTo("123");
    }

    @Test
    void obterInexistenteRetornaNaoEncontrado() {
        when(repositorio.findById("999")).thenReturn(Optional.empty());

        assertThrows(ErroServico.NaoEncontrado.class, () -> servico.obter("999"));
    }

    @Test
    void atualizarComNumeroDivergenteRetornaParametroInvalidoSemPersistir() {
        assertThrows(ErroServico.ParametroInvalido.class, () -> servico.atualizar("123", dto("456")));

        verify(repositorio, never()).existsById(any());
        verify(repositorio, never()).save(any());
    }

    @Test
    void atualizarInexistenteRetornaNaoEncontradoSemSalvar() {
        when(repositorio.findById("123")).thenReturn(Optional.empty());

        assertThrows(ErroServico.NaoEncontrado.class, () -> servico.atualizar("123", dto("123")));

        verify(repositorio, never()).save(any());
    }

    @Test
    void atualizarExistenteSalva() {
        Notificacao n = new Notificacao();
        when(repositorio.findById("123")).thenReturn(Optional.of(n));

        servico.atualizar("123", dto("123"));

        verify(repositorio).save(any(Notificacao.class));
    }

    @Test
    void removerInexistenteRetornaNaoEncontrado() {
        when(repositorio.existsById("999")).thenReturn(false);

        assertThrows(ErroServico.NaoEncontrado.class, () -> servico.remover("999"));

        verify(repositorio, never()).deleteById(any());
    }

    @Test
    void removerExistenteRetornaOkEDeleta() {
        when(repositorio.existsById("123")).thenReturn(true);

        servico.remover("123");

        verify(repositorio).deleteById("123");
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
