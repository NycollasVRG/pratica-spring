package com.pratica.notificacao.validation;

import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.dto.request.EnderecoRequestDTO;
import com.pratica.notificacao.dto.request.PacienteRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegrasPacienteTest {

    private final Validator validador = ValidadorTestSupport.criarValidador();

    @Test
    void femininoAdultoSemGestanteGeraErroEmGestante() {
        var violacoes = validar(paciente(Sexo.FEMININO, 30, null, null));

        assertThat(caminhos(violacoes, "gestante")).hasSize(1);
        assertThat(mensagens(violacoes, "gestante"))
                .first()
                .isEqualTo("O período gestacional é obrigatório para pacientes do sexo feminino");
    }

    @Test
    void femininoMenorDeSeteAnosSemGestanteEhValido() {
        assertThat(validar(paciente(Sexo.FEMININO, 6, null, null))).isEmpty();
    }

    @Test
    void femininoMenorDeSeteAnosComNaoSeAplicaEhValido() {
        assertThat(validar(paciente(Sexo.FEMININO, 6, null, PeriodoGestacional.NAO_SE_APLICA))).isEmpty();
    }

    @Test
    void femininoMenorDeSeteAnosComGestanteInvalidaGeraErro() {
        var violacoes = validar(paciente(Sexo.FEMININO, 6, null, PeriodoGestacional.PRIMEIRO_TRIMESTRE));

        assertThat(caminhos(violacoes, "gestante")).hasSize(1);
        assertThat(mensagens(violacoes, "gestante"))
                .first()
                .isEqualTo("Para menores de 7 anos o período gestacional deve ser Não se aplica");
    }

    @Test
    void femininoDeSeteAnosSemGestanteGeraErro() {
        var violacoes = validar(paciente(Sexo.FEMININO, 7, null, null));

        assertThat(caminhos(violacoes, "gestante")).hasSize(1);
    }

    @Test
    void masculinoSemGestanteEhValido() {
        assertThat(validar(paciente(Sexo.MASCULINO, 40, null, null))).isEmpty();
    }

    @Test
    void masculinoComNaoSeAplicaEhValido() {
        assertThat(validar(paciente(Sexo.MASCULINO, 40, null, PeriodoGestacional.NAO_SE_APLICA))).isEmpty();
    }

    @Test
    void masculinoComGestanteNaoGeraErro() {
        var violacoes = validar(paciente(Sexo.MASCULINO, 40, null, PeriodoGestacional.NAO));

        assertThat(caminhos(violacoes, "gestante")).hasSize(1);
        assertThat(mensagens(violacoes, "gestante"))
                .first()
                .isEqualTo("O período gestacional só se aplica a pacientes do sexo feminino");
    }

    @Test
    void semIdadeENascimentoGeraErroEmIdade() {
        var violacoes = validar(paciente(Sexo.MASCULINO, null, null, null));

        assertThat(caminhos(violacoes, "idade")).hasSize(1);
        assertThat(mensagens(violacoes, "idade"))
                .first()
                .isEqualTo("A idade é obrigatória quando a data de nascimento não é informada");
    }

    @Test
    void idadeCalculadaPelaDataDeNascimento() {
        assertThat(validar(paciente(Sexo.MASCULINO, null, LocalDate.now().minusYears(30), null))).isEmpty();
    }

    @Test
    void idadeInformadaSemNascimentoEhValida() {
        assertThat(validar(paciente(Sexo.MASCULINO, 42, null, null))).isEmpty();
    }

    @Test
    void nascimentoRecenteIdentificaMenorDeSete() {
        assertThat(validar(paciente(Sexo.FEMININO, null, LocalDate.now().minusYears(5), null))).isEmpty();
    }

    @Test
    void nascimentoDeSeteAnosExigeGestanteNoFeminino() {
        var violacoes = validar(paciente(Sexo.FEMININO, null, LocalDate.now().minusYears(7), null));

        assertThat(caminhos(violacoes, "gestante")).hasSize(1);
    }

    @Test
    void sexoNuloNaoAcumulaErrosDeGestante() {
        var violacoes = validar(paciente(null, 30, null, null));

        assertThat(caminhos(violacoes, "sexo")).hasSize(1);
        assertThat(caminhos(violacoes, "gestante")).isEmpty();
    }

    @Test
    void idadeAcimaDeCentoECinquentaGeraErro() {
        var violacoes = validar(paciente(Sexo.MASCULINO, 200, null, null));

        assertThat(caminhos(violacoes, "idade")).hasSize(1);
        assertThat(mensagens(violacoes, "idade")).first().isEqualTo("A idade deve ser no máximo 150 anos");
    }

    @Test
    void idadeNegativaGeraErro() {
        var violacoes = validar(paciente(Sexo.MASCULINO, -1, null, null));

        assertThat(caminhos(violacoes, "idade")).hasSize(1);
        assertThat(mensagens(violacoes, "idade")).first().isEqualTo("A idade não pode ser negativa");
    }

    @Test
    void cartaoSusInvalidoGeraErro() {
        PacienteRequestDTO dto = new PacienteRequestDTO(
                "Maria Silva", null, 30, Sexo.FEMININO, PeriodoGestacional.NAO,
                null, null, "123", "Ana Maria", null, enderecoValido());

        var violacoes = validar(dto);

        assertThat(caminhos(violacoes, "numeroCartaoSus")).hasSize(1);
        assertThat(mensagens(violacoes, "numeroCartaoSus"))
                .first()
                .isEqualTo("O número do Cartão SUS deve ter exatamente 15 dígitos");
    }

    // ------------------------------------------------------------------

    private Set<ConstraintViolation<PacienteRequestDTO>> validar(PacienteRequestDTO dto) {
        return validador.validate(dto);
    }

    private static PacienteRequestDTO paciente(
            Sexo sexo, Integer idade, LocalDate nascimento, PeriodoGestacional gestante) {
        return new PacienteRequestDTO(
                "Maria Silva", nascimento, idade, sexo, gestante,
                null, null, null, "Ana Maria", null, enderecoValido());
    }

    private static EnderecoRequestDTO enderecoValido() {
        return new EnderecoRequestDTO(
                "PB", "João Pessoa", null, null, null, null, null,
                null, null, null, "58000000", null, "Brasil");
    }

    private static List<String> caminhos(Set<ConstraintViolation<PacienteRequestDTO>> violacoes, String campo) {
        return violacoes.stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .filter(campo::equals)
                .toList();
    }

    private static List<String> mensagens(Set<ConstraintViolation<PacienteRequestDTO>> violacoes, String campo) {
        return violacoes.stream()
                .filter(violacao -> violacao.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
