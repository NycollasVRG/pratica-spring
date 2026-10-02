package com.pratica.notificacao.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pratica.notificacao.dto.request.NotificacaoFilterDTO;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.service.NotificacaoService;

@RestController
@RequestMapping("/notificacao")
public class NotificacaoController {

    private final NotificacaoService servico;

    public NotificacaoController(NotificacaoService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<NotificacaoResponseDTO> criar(@Valid @RequestBody NotificacaoRequestDTO dto) {
        NotificacaoResponseDTO corpo = servico.criar(dto);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numero}")
                .buildAndExpand(corpo.numeroNotificacao())
                .toUri();
        return ResponseEntity.created(localizacao).body(corpo);
    }

    @GetMapping
    public ResponseEntity<PaginaRespostaDTO<NotificacaoResponseDTO>> listar(
            NotificacaoFilterDTO filtro,
            @PageableDefault(size = 10, sort = "dataNotificacao", direction = Sort.Direction.DESC) Pageable pageable) {

        PaginaRespostaDTO<NotificacaoResponseDTO> resultado = servico.listar(filtro, pageable);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<NotificacaoResponseDTO> obter(@PathVariable String numero) {
        return ResponseEntity.ok(servico.obter(numero));
    }

    @PutMapping("/{numero}")
    public ResponseEntity<NotificacaoResponseDTO> atualizar(
            @PathVariable String numero, @Valid @RequestBody NotificacaoRequestDTO dto) {
        return ResponseEntity.ok(servico.atualizar(numero, dto));
    }

    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> remover(@PathVariable String numero) {
        servico.remover(numero);
        return ResponseEntity.noContent().build();
    }
}
