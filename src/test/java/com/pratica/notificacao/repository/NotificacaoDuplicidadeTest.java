package com.pratica.notificacao.repository;

import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.specification.NotificacaoSpecs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RN01 — filtro "duplicadas=true" (apenas na listagem): EXISTS correlacionado
 * com agravo igual ignorando caixa/espaços, janela de ±3 dias inclusiva e
 * agravo em branco/nulo excluído.
 */
@DataJpaTest
class NotificacaoDuplicidadeTest {

    @Autowired
    private NotificacaoRepository repositorio;

    @Test
    void mesmaJanelaDeTresDiasRetornaAmbas() {
        salvar("1", "Febre Alta", LocalDate.of(2026, 1, 10));
        salvar("2", "FEBRE ALTA", LocalDate.of(2026, 1, 12));

        assertThat(duplicadas()).extracting(Notificacao::getNumeroNotificacao)
                .containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void bordaExataDeTresDiasContaComoDuplicata() {
        salvar("1", "Febre", LocalDate.of(2026, 1, 10));
        salvar("2", "Febre", LocalDate.of(2026, 1, 13));

        assertThat(duplicadas()).extracting(Notificacao::getNumeroNotificacao)
                .containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void quatroDiasJaFicaForaDaJanela() {
        salvar("1", "Febre", LocalDate.of(2026, 1, 10));
        salvar("2", "Febre", LocalDate.of(2026, 1, 14));

        assertThat(duplicadas()).isEmpty();
    }

    @Test
    void comparacaoIgnoraCaixa() {
        salvar("1", "Febre", LocalDate.of(2026, 1, 10));
        salvar("2", "fEbRe", LocalDate.of(2026, 1, 10));

        assertThat(duplicadas()).extracting(Notificacao::getNumeroNotificacao)
                .containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void agravoEmBrancoFicaDeFora() {
        salvar("1", "", LocalDate.of(2026, 1, 10));
        salvar("2", "", LocalDate.of(2026, 1, 11));
        salvar("3", "   ", LocalDate.of(2026, 1, 11));

        assertThat(duplicadas()).isEmpty();
    }

    @Test
    void agravoNuloFicaDeFora() {
        salvar("1", null, LocalDate.of(2026, 1, 10));
        salvar("2", null, LocalDate.of(2026, 1, 11));

        assertThat(duplicadas()).isEmpty();
    }

    @Test
    void agravoDiferenteNaoDuplica() {
        salvar("1", "Febre", LocalDate.of(2026, 1, 10));
        salvar("2", "Tosse", LocalDate.of(2026, 1, 11));

        assertThat(duplicadas()).isEmpty();
    }

    @Test
    void notificacaoSemDuplicataNaoAparece() {
        salvar("1", "Febre", LocalDate.of(2026, 1, 10));
        salvar("2", "Sarampo", LocalDate.of(2026, 1, 11));

        assertThat(duplicadas()).isEmpty();
    }

    @Test
    void compoeComOutrosFiltros() {
        salvar("1", "Febre", LocalDate.of(2026, 1, 10), "PB");
        salvar("2", "Febre", LocalDate.of(2026, 1, 11), "PB");
        salvar("3", "Febre", LocalDate.of(2026, 1, 11), "SP");
        salvar("4", "Febre", LocalDate.of(2026, 1, 12), "SP");

        Specification<Notificacao> combinada =
                NotificacaoSpecs.comFiltros("PB", null, null, null, null, null)
                        .and(NotificacaoSpecs.comAgravoDuplicado());

        assertThat(repositorio.findAll(combinada))
                .extracting(Notificacao::getNumeroNotificacao)
                .containsExactlyInAnyOrder("1", "2");
    }

    // ------------------------------------------------------------------

    private List<Notificacao> duplicadas() {
        return repositorio.findAll(NotificacaoSpecs.comAgravoDuplicado());
    }

    private Notificacao salvar(String numero, String agravo, LocalDate data) {
        return salvar(numero, agravo, data, "PB");
    }

    private Notificacao salvar(String numero, String agravo, LocalDate data, String uf) {
        Notificacao notificacao = new Notificacao();
        notificacao.setNumeroNotificacao(numero);
        notificacao.setAgravoDoenca(agravo);
        notificacao.setDataNotificacao(data);
        notificacao.setUfNotificacao(uf);
        notificacao.setMunicipioNotificacao("João Pessoa");
        notificacao.setTipoNotificacao(TipoNotificacao.INDIVIDUAL);

        Paciente paciente = new Paciente();
        paciente.setNomePaciente("Paciente " + numero);
        notificacao.setPaciente(paciente);

        return repositorio.saveAndFlush(notificacao);
    }
}
