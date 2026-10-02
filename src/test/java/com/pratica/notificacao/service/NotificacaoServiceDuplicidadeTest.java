package com.pratica.notificacao.service;

import com.pratica.notificacao.common.ClockConfig;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.dto.request.NotificacaoFilterDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.mapper.NotificacaoMapper;
import com.pratica.notificacao.mapper.NotificacaoMapperImpl;
import com.pratica.notificacao.repository.NotificacaoRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({NotificacaoService.class, ClockConfig.class, NotificacaoMapperImpl.class})
class NotificacaoServiceDuplicidadeTest {

    @Autowired
    private NotificacaoRepository repositorio;

    @Autowired
    private NotificacaoService servico;

    @Test
    void duplicadasTrueRetornaSomenteAsComDuplicata() {
        semear();

        PaginaRespostaDTO<NotificacaoResponseDTO> envelope = listar(true);

        assertThat(envelope.conteudo())
                .extracting(NotificacaoResponseDTO::numeroNotificacao)
                .containsExactly("1", "2");
    }

    @Test
    void duplicadasFalseRetornaTodas() {
        semear();

        PaginaRespostaDTO<NotificacaoResponseDTO> envelope = listar(false);

        assertThat(envelope.conteudo())
                .extracting(NotificacaoResponseDTO::numeroNotificacao)
                .containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    void duplicadasTrueComposComFiltroDeUf() {
        semear();

        NotificacaoFilterDTO filtro = new NotificacaoFilterDTO("SP", null, null, null, null, null, true);
        PaginaRespostaDTO<NotificacaoResponseDTO> envelope = servico.listar(filtro, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "numeroNotificacao")));

        assertThat(envelope.conteudo()).isEmpty();
    }

    // ------------------------------------------------------------------

    private PaginaRespostaDTO<NotificacaoResponseDTO> listar(boolean duplicadas) {
        NotificacaoFilterDTO filtro = new NotificacaoFilterDTO(null, null, null, null, null, null, duplicadas);
        return servico.listar(filtro, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "numeroNotificacao")));
    }

    private void semear() {
        salvar("1", "Febre Alta", LocalDate.of(2026, 1, 10), "PB");
        salvar("2", "FEBRE ALTA", LocalDate.of(2026, 1, 11), "PB");
        salvar("3", "Febre Alta", LocalDate.of(2026, 1, 20), "PB");
        salvar("4", "Tosse", LocalDate.of(2026, 1, 10), "SP");
        salvar("5", "Sarampo", LocalDate.of(2026, 1, 12), "SP");
    }

    private void salvar(String numero, String agravo, LocalDate data, String uf) {
        Notificacao notificacao = new Notificacao();
        notificacao.setNumeroNotificacao(numero);
        notificacao.setAgravoDoenca(agravo);
        notificacao.setDataNotificacao(data);
        notificacao.setUfNotificacao(uf);
        notificacao.setMunicipioNotificacao("Município " + uf);
        notificacao.setTipoNotificacao(TipoNotificacao.INDIVIDUAL);

        Paciente paciente = new Paciente();
        paciente.setNomePaciente("Paciente " + numero);
        notificacao.setPaciente(paciente);

        repositorio.saveAndFlush(notificacao);
    }
}
