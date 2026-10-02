package com.pratica.notificacao.repository;

import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.specification.NotificacaoSpecs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Filtros, paginação e ordenação das Specifications contra H2 (sem Docker).
 */
@DataJpaTest
class NotificacaoRepositoryFiltrosTest {

    @Autowired
    private NotificacaoRepository repositorio;

    @Test
    void filtraPorUfIgnorandoCaixaEDuplicatas() {
        salvar("1", LocalDate.of(2026, 1, 10), "PB", "João Pessoa");
        salvar("2", LocalDate.of(2026, 1, 11), "SP", "São Paulo");
        salvar("3", LocalDate.of(2026, 1, 12), "PB", "Campina Grande");

        List<Notificacao> resultado = repositorio.findAll(
                NotificacaoSpecs.comFiltros("pb", null, null, null, null, null));

        assertThat(resultado)
                .extracting(Notificacao::getNumeroNotificacao)
                .containsExactlyInAnyOrder("1", "3");
    }

    @Test
    void filtraPorMunicipioIgnorandoCaixa() {
        salvar("1", LocalDate.of(2026, 1, 10), "PB", "João Pessoa");
        salvar("2", LocalDate.of(2026, 1, 11), "PB", "Campina Grande");

        List<Notificacao> resultado = repositorio.findAll(
                NotificacaoSpecs.comFiltros(null, "  joão PESSOA ", null, null, null, null));

        assertThat(resultado)
                .extracting(Notificacao::getNumeroNotificacao)
                .containsExactly("1");
    }

    @Test
    void filtraPorTipoESexo() {
        salvar("1", LocalDate.of(2026, 1, 10), "PB", "João Pessoa");
        Notificacao surtoMasc = salvar("2", LocalDate.of(2026, 1, 11), "SP", "São Paulo");
        surtoMasc.setTipoNotificacao(TipoNotificacao.SURTO);
        surtoMasc.getPaciente().setSexo(Sexo.MASCULINO);
        repositorio.saveAndFlush(surtoMasc);

        List<Notificacao> resultado = repositorio.findAll(
                NotificacaoSpecs.comFiltros(null, null, TipoNotificacao.SURTO, Sexo.MASCULINO, null, null));

        assertThat(resultado)
                .extracting(Notificacao::getNumeroNotificacao)
                .containsExactly("2");
    }

    @Test
    void filtraPorPeriodoInclusivoNasBordas() {
        salvar("1", LocalDate.of(2026, 1, 9), "PB", "João Pessoa");
        salvar("2", LocalDate.of(2026, 1, 10), "PB", "João Pessoa");
        salvar("3", LocalDate.of(2026, 1, 12), "PB", "João Pessoa");
        salvar("4", LocalDate.of(2026, 1, 13), "PB", "João Pessoa");

        List<Notificacao> resultado = repositorio.findAll(
                NotificacaoSpecs.comFiltros(null, null, null, null,
                        LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 12)));

        assertThat(resultado)
                .extracting(Notificacao::getNumeroNotificacao)
                .containsExactlyInAnyOrder("2", "3");
    }

    @Test
    void semFiltrosRetornaTudo() {
        salvar("1", LocalDate.of(2026, 1, 10), "PB", "João Pessoa");
        salvar("2", LocalDate.of(2026, 1, 11), "SP", "São Paulo");

        List<Notificacao> resultado = repositorio.findAll(
                NotificacaoSpecs.comFiltros(null, null, null, null, null, null));

        assertThat(resultado).hasSize(2);
    }

    @Test
    void paginaEOrdenaPorNomeDoPacienteAninhado() {
        for (int i = 1; i <= 12; i++) {
            String numero = String.format("%02d", i);
            Notificacao notificacao = salvar(numero, LocalDate.of(2026, 1, 10), "PB", "João Pessoa");
            notificacao.getPaciente().setNomePaciente("Paciente " + numero);
            repositorio.saveAndFlush(notificacao);
        }

        Page<Notificacao> pagina = repositorio.findAll(
                NotificacaoSpecs.comFiltros(null, null, null, null, null, null),
                PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "paciente.nomePaciente")));

        assertThat(pagina.getTotalElements()).isEqualTo(12);
        assertThat(pagina.getTotalPages()).isEqualTo(3);
        assertThat(pagina.getContent())
                .extracting(Notificacao::getNumeroNotificacao)
                .containsExactly("06", "07", "08", "09", "10");
    }

    // ------------------------------------------------------------------

    private Notificacao salvar(String numero, LocalDate data, String uf, String municipio) {
        Notificacao notificacao = new Notificacao();
        notificacao.setNumeroNotificacao(numero);
        notificacao.setAgravoDoenca("Febre");
        notificacao.setDataNotificacao(data);
        notificacao.setUfNotificacao(uf);
        notificacao.setMunicipioNotificacao(municipio);
        notificacao.setTipoNotificacao(TipoNotificacao.INDIVIDUAL);

        Paciente paciente = new Paciente();
        paciente.setNomePaciente("Paciente " + numero);
        paciente.setSexo(Sexo.FEMININO);
        notificacao.setPaciente(paciente);

        return repositorio.saveAndFlush(notificacao);
    }
}
