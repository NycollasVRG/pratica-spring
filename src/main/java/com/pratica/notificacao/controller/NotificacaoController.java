package com.pratica.notificacao.controller;

import java.net.URI;
import java.time.LocalDate;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pratica.notificacao.common.Result;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.exception.Problemas;
import com.pratica.notificacao.service.NotificacaoService;

@RestController
@RequestMapping("/notificacao")
public class NotificacaoController {

    private final NotificacaoService servico;

    public NotificacaoController(NotificacaoService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<Object> criar(@Valid @RequestBody NotificacaoRequestDTO dto) {
        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.criar(dto);
        if (resultado instanceof Result.Ok<?, ?> ok) {
            NotificacaoResponseDTO corpo = (NotificacaoResponseDTO) ok.value();
            URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{numero}")
                    .buildAndExpand(corpo.numeroNotificacao())
                    .toUri();
            return ResponseEntity.created(localizacao).body(corpo);
        }
        return Problemas.resposta(erroDe(resultado));
    }

    @GetMapping
    public ResponseEntity<Object> listar(
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) TipoNotificacao tipo,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "false") boolean duplicadas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(defaultValue = "dataNotificacao") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direcao) {

        Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> resultado = servico.listar(
                uf, municipio, tipo, sexo, dataInicio, dataFim, duplicadas,
                pagina, tamanho, ordenarPor, direcao);
        if (resultado instanceof Result.Ok<?, ?> ok) {
            return ResponseEntity.ok(ok.value());
        }
        return Problemas.resposta(erroDe(resultado));
    }

    @GetMapping("/{numero}")
    public ResponseEntity<Object> obter(@PathVariable String numero) {
        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.obter(numero);
        if (resultado instanceof Result.Ok<?, ?> ok) {
            return ResponseEntity.ok(ok.value());
        }
        return Problemas.resposta(erroDe(resultado));
    }

    @PutMapping("/{numero}")
    public ResponseEntity<Object> atualizar(
            @PathVariable String numero, @Valid @RequestBody NotificacaoRequestDTO dto) {
        Result<NotificacaoResponseDTO, ErroServico> resultado = servico.atualizar(numero, dto);
        if (resultado instanceof Result.Ok<?, ?> ok) {
            return ResponseEntity.ok(ok.value());
        }
        return Problemas.resposta(erroDe(resultado));
    }

    @DeleteMapping("/{numero}")
    public ResponseEntity<Object> remover(@PathVariable String numero) {
        Result<Void, ErroServico> resultado = servico.remover(numero);
        if (resultado instanceof Result.Ok<?, ?>) {
            return ResponseEntity.noContent().build();
        }
        return Problemas.resposta(erroDe(resultado));
    }

    @SuppressWarnings("unchecked")
    private static ErroServico erroDe(Result<?, ErroServico> resultado) {
        return ((Result.Err<?, ErroServico>) resultado).error();
    }
}
