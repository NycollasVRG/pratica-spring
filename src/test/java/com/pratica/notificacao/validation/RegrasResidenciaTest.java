package com.pratica.notificacao.validation;

import com.pratica.notificacao.dto.request.EnderecoRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegrasResidenciaTest {

    private final Validator validador = ValidadorTestSupport.criarValidador();

    @Test
    void paisNuloContaComoBrasilEExigeUf() {
        var violacoes = validar(endereco(null, null, null));

        assertThat(caminhos(violacoes, "ufResidencia")).hasSize(1);
        assertThat(mensagens(violacoes, "ufResidencia"))
                .first()
                .isEqualTo("A UF de residência é obrigatória para pacientes que residem no Brasil");
    }

    @Test
    void paisEmBrancoContaComoBrasilEExigeUf() {
        var violacoes = validar(endereco(null, null, "   "));

        assertThat(caminhos(violacoes, "ufResidencia")).hasSize(1);
    }

    @Test
    void brasilComUfEMunicipioEhValido() {
        assertThat(validar(endereco("PB", "João Pessoa", "Brasil"))).isEmpty();
    }

    @Test
    void brasilSemMunicipioGeraErroEmMunicipio() {
        var violacoes = validar(endereco("PB", null, "Brasil"));

        assertThat(caminhos(violacoes, "municipioResidencia")).hasSize(1);
        assertThat(mensagens(violacoes, "municipioResidencia"))
                .first()
                .isEqualTo("O município de residência é obrigatório quando a UF é informada");
    }

    @Test
    void exteriorSemUfEMunicipioEhValido() {
        assertThat(validar(endereco(null, null, "Argentina"))).isEmpty();
    }

    @Test
    void exteriorComUfSemMunicipioGeraErroEmMunicipio() {
        var violacoes = validar(endereco("RS", null, "Argentina"));

        assertThat(caminhos(violacoes, "municipioResidencia")).hasSize(1);
    }

    @Test
    void exteriorComUfComMunicipioEhValido() {
        assertThat(validar(endereco("RS", "Porto Alegre", "Argentina"))).isEmpty();
    }

    @Test
    void brasilComCaixaEEspacosExtrasAindaEhBrasil() {
        var violacoes = validar(endereco(null, null, "  BRASIL  "));

        assertThat(caminhos(violacoes, "ufResidencia")).hasSize(1);
    }

    @Test
    void brasilComAcentoAindaEhBrasil() {
        var violacoes = validar(endereco(null, null, "Brasíl"));

        assertThat(caminhos(violacoes, "ufResidencia")).hasSize(1);
    }

    @Test
    void ufValidaEhAceita() {
        assertThat(validar(endereco("PB", "João Pessoa", "Brasil"))).isEmpty();
    }

    @Test
    void ufComLetraMinusculaGeraErro() {
        var violacoes = validar(endereco("pb", "João Pessoa", "Brasil"));

        assertThat(caminhos(violacoes, "ufResidencia")).hasSize(1);
        assertThat(mensagens(violacoes, "ufResidencia"))
                .first()
                .isEqualTo("UF inválida; use uma das 27 siglas em maiúsculas (ex: PB)");
    }

    @Test
    void ufInexistenteGeraErro() {
        var violacoes = validar(endereco("XX", "João Pessoa", "Brasil"));

        assertThat(caminhos(violacoes, "ufResidencia")).hasSize(1);
    }

    @Test
    void ufForaDoPadraoComMunicipioEhValida() {
        assertThat(validar(endereco("SP", "São Paulo", "Brasil"))).isEmpty();
    }

    @Test
    void cepInvalidoGeraErro() {
        var violacoes = validar(endereco("PB", "João Pessoa", "Brasil", "123"));

        assertThat(caminhos(violacoes, "cepResidencia")).hasSize(1);
        assertThat(mensagens(violacoes, "cepResidencia"))
                .first()
                .isEqualTo("O CEP deve ter 8 dígitos (com ou sem hífen)");
    }

    @Test
    void cepComHifenEhValido() {
        assertThat(validar(endereco("PB", "João Pessoa", "Brasil", "58000-000"))).isEmpty();
    }

    @Test
    void cepSemHifenEhValido() {
        assertThat(validar(endereco("PB", "João Pessoa", "Brasil", "58000000"))).isEmpty();
    }

    // ------------------------------------------------------------------

    private Set<ConstraintViolation<EnderecoRequestDTO>> validar(EnderecoRequestDTO dto) {
        return validador.validate(dto);
    }

    private static EnderecoRequestDTO endereco(String uf, String municipio, String pais) {
        return endereco(uf, municipio, pais, null);
    }

    private static EnderecoRequestDTO endereco(String uf, String municipio, String pais, String cep) {
        return new EnderecoRequestDTO(
                uf, municipio, null, null, null, null, null,
                null, null, null, cep, null, pais);
    }

    private static List<String> caminhos(Set<ConstraintViolation<EnderecoRequestDTO>> violacoes, String campo) {
        return violacoes.stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .filter(campo::equals)
                .toList();
    }

    private static List<String> mensagens(Set<ConstraintViolation<EnderecoRequestDTO>> violacoes, String campo) {
        return violacoes.stream()
                .filter(violacao -> violacao.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
