package com.pratica.notificacao.controller;

import com.pratica.notificacao.common.Result;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.exception.ViolacaoCampo;
import com.pratica.notificacao.service.NotificacaoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacaoController.class)
class NotificacaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NotificacaoService servico;

    @Test
    void postRetorna201ComLocation() throws Exception {
        when(servico.criar(any())).thenReturn(Result.ok(resposta("123")));

        mvc.perform(post("/notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido("123")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/notificacao/123")))
                .andExpect(jsonPath("$.numeroNotificacao").value("123"));
    }

    @Test
    void postDuplicadoRetorna409ProblemDetail() throws Exception {
        when(servico.criar(any())).thenReturn(Result.err(
                new ErroServico.Conflito("Já existe uma notificação com o número 123")));

        mvc.perform(post("/notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido("123")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:conflito"))
                .andExpect(jsonPath("$.title").value("Conflito"))
                .andExpect(jsonPath("$.detail").value("Já existe uma notificação com o número 123"))
                .andExpect(jsonPath("$.instance").isNotEmpty());
    }

    @Test
    void postInvalidoRetorna400ComListaDeErrors() throws Exception {
        mvc.perform(post("/notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemNomePaciente()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:requisicao-invalida"))
                .andExpect(jsonPath("$.errors[0].campo").value("paciente.nomePaciente"))
                .andExpect(jsonPath("$.errors[0].mensagem")
                        .value("O nome do paciente é obrigatório"));
    }

    @Test
    void postComEnumInvalidoRetorna400() throws Exception {
        mvc.perform(post("/notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido("123").replace("\"INDIVIDUAL\"", "\"XYZ\"")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:requisicao-invalida"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void postComJsonIlegivelRetorna400() throws Exception {
        mvc.perform(post("/notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{isso nao e json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(containsString("Corpo da requisição")));
    }

    @Test
    void getRetorna200ComCorpo() throws Exception {
        when(servico.obter("123")).thenReturn(Result.ok(resposta("123")));

        mvc.perform(get("/notificacao/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroNotificacao").value("123"));
    }

    @Test
    void getInexistenteRetorna404ProblemDetail() throws Exception {
        when(servico.obter("999")).thenReturn(Result.err(
                new ErroServico.NaoEncontrado("Notificação 999 não encontrada")));

        mvc.perform(get("/notificacao/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:nao-encontrado"))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value(containsString("999")));
    }

    @Test
    void putComNumeroDivergenteRetorna400ComErrors() throws Exception {
        when(servico.atualizar(any(), any())).thenReturn(Result.err(
                new ErroServico.ParametroInvalido(List.of(
                        new ViolacaoCampo("numeroNotificacao",
                                "O número do corpo da requisição difere do número da URL")))));

        mvc.perform(put("/notificacao/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido("456")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[0].campo").value("numeroNotificacao"));
    }

    @Test
    void putValidoRetorna200() throws Exception {
        when(servico.atualizar(any(), any())).thenReturn(Result.ok(resposta("123")));

        mvc.perform(put("/notificacao/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido("123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroNotificacao").value("123"));
    }

    @Test
    void deleteRetorna204() throws Exception {
        when(servico.remover("123")).thenReturn(Result.ok(null));

        mvc.perform(delete("/notificacao/123"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void deleteInexistenteRetorna404() throws Exception {
        when(servico.remover("999")).thenReturn(Result.err(
                new ErroServico.NaoEncontrado("Notificação 999 não encontrada")));

        mvc.perform(delete("/notificacao/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:nao-encontrado"));
    }

    @Test
    void metodoNaoSuportadoRetorna405ProblemDetail() throws Exception {
        mvc.perform(delete("/notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:metodo-nao-suportado"))
                .andExpect(jsonPath("$.title").value("Método não suportado"));
    }

    @Test
    void contentTypeNaoSuportadoRetorna415ProblemDetail() throws Exception {
        mvc.perform(post("/notificacao")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("texto qualquer"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:conteudo-nao-suportado"));
    }

    @Test
    void getListRetorna200ComEnvelope() throws Exception {
        when(servico.listar(any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any()))
                .thenReturn(Result.ok(new PaginaRespostaDTO<>(List.of(resposta("1")), 0, 10, 1, 1)));

        mvc.perform(get("/notificacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].numeroNotificacao").value("1"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(10))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void getListComOrdenacaoInvalidaRetorna400ProblemDetail() throws Exception {
        when(servico.listar(any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any()))
                .thenReturn(Result.err(new ErroServico.ParametroInvalido(List.of(
                        new ViolacaoCampo("ordenarPor", "Ordenação inválida; use uma de: dataNotificacao")))));

        mvc.perform(get("/notificacao?ordenarPor=qualquerCoisa"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[0].campo").value("ordenarPor"));
    }

    @Test
    void getListComTipoInvalidoRetorna400ComValoresAceitos() throws Exception {
        mvc.perform(get("/notificacao?tipo=XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:pratica:notificacao:requisicao-invalida"))
                .andExpect(jsonPath("$.errors[0].campo").value("tipo"))
                .andExpect(jsonPath("$.errors[0].mensagem").value(containsString("INDIVIDUAL")));
    }

    @Test
    void getListComDataInvalidaRetorna400() throws Exception {
        mvc.perform(get("/notificacao?dataInicio=naoEdata"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("dataInicio"))
                .andExpect(jsonPath("$.errors[0].mensagem").value(containsString("AAAA-MM-DD")));
    }

    @Test
    void getListEncaminhaDuplicadasTrueParaOServico() throws Exception {
        when(servico.listar(any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any()))
                .thenReturn(Result.ok(new PaginaRespostaDTO<>(List.of(), 0, 10, 0, 0)));

        mvc.perform(get("/notificacao?duplicadas=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0));

        verify(servico).listar(any(), any(), any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.eq(true), anyInt(), anyInt(), any(), any());
    }

    // ------------------------------------------------------------------

    private static NotificacaoResponseDTO resposta(String numero) {
        return new NotificacaoResponseDTO(numero, null, null, null, null, null, null, null, null, null);
    }

    private static String jsonValido(String numero) {
        return """
                {
                  "numeroNotificacao": "%s",
                  "tipoNotificacao": "INDIVIDUAL",
                  "agravoDoenca": "Febre",
                  "dataNotificacao": "2026-01-10",
                  "ufNotificacao": "PB",
                  "municipioNotificacao": "João Pessoa",
                  "unidadeSaudeNotificadora": "UBS Centro",
                  "dataPrimeirosSintomas": "2026-01-05",
                  "paciente": {
                    "nomePaciente": "Maria Silva",
                    "dataNascimento": "1990-05-20",
                    "sexo": "FEMININO",
                    "gestante": "NAO",
                    "nomeMae": "Ana Maria",
                    "endereco": {
                      "ufResidencia": "PB",
                      "municipioResidencia": "João Pessoa",
                      "paisResidencia": "Brasil"
                    }
                  },
                  "investigacao": null
                }
                """.formatted(numero);
    }

    private static String jsonSemNomePaciente() {
        return jsonValido("123").replace("\"nomePaciente\": \"Maria Silva\",", "");
    }
}
