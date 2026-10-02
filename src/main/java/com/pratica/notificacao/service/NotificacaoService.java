package com.pratica.notificacao.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pratica.notificacao.common.Result;
import com.pratica.notificacao.common.Textos;
import com.pratica.notificacao.domain.Endereco;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.domain.enums.TipoNotificacao;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.exception.ViolacaoCampo;
import com.pratica.notificacao.mapper.NotificacaoMapper;
import com.pratica.notificacao.repository.NotificacaoRepository;
import com.pratica.notificacao.specification.NotificacaoSpecs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;


@Service
public class NotificacaoService {

    private final NotificacaoRepository repositorio;
    private final Clock relogio;

    public NotificacaoService(NotificacaoRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    @Transactional
    public Result<NotificacaoResponseDTO, ErroServico> criar(NotificacaoRequestDTO dto) {
        if (repositorio.existsById(dto.numeroNotificacao())) {
            return Result.err(new ErroServico.Conflito(
                    "Já existe uma notificação com o número " + dto.numeroNotificacao()));
        }
        Notificacao entidade = NotificacaoMapper.paraEntidade(dto);
        aplicarRegrasDeEscrita(entidade);
        repositorio.save(entidade);
        return Result.ok(NotificacaoMapper.paraResposta(entidade));
    }

    @Transactional(readOnly = true)
    public Result<NotificacaoResponseDTO, ErroServico> obter(String numero) {
        return repositorio.findById(numero)
                .map(NotificacaoMapper::paraResposta)
                .<Result<NotificacaoResponseDTO, ErroServico>>map(Result::ok)
                .orElseGet(() -> Result.err(naoEncontrada(numero)));
    }

    @Transactional
    public Result<NotificacaoResponseDTO, ErroServico> atualizar(String numero, NotificacaoRequestDTO dto) {
        if (!numero.equals(dto.numeroNotificacao())) {
            return Result.err(parametroInvalido("numeroNotificacao",
                    "O número do corpo da requisição difere do número da URL"));
        }
        if (!repositorio.existsById(numero)) {
            return Result.err(naoEncontrada(numero));
        }
        Notificacao entidade = NotificacaoMapper.paraEntidade(dto);
        aplicarRegrasDeEscrita(entidade);
        repositorio.save(entidade);
        return Result.ok(NotificacaoMapper.paraResposta(entidade));
    }

    @Transactional
    public Result<Void, ErroServico> remover(String numero) {
        if (!repositorio.existsById(numero)) {
            return Result.err(naoEncontrada(numero));
        }
        repositorio.deleteById(numero);
        return Result.ok(null);
    }

   
    @Transactional(readOnly = true)
    public Result<PaginaRespostaDTO<NotificacaoResponseDTO>, ErroServico> listar(
            String uf, String municipio, TipoNotificacao tipo, Sexo sexo,
            LocalDate dataInicio, LocalDate dataFim, boolean duplicadas,
            int pagina, int tamanho, String ordenarPor, String direcao) {

        if (pagina < 0) {
            return Result.err(parametroInvalido("pagina", "A página inicial não pode ser negativa"));
        }
        if (tamanho < 1 || tamanho > 100) {
            return Result.err(parametroInvalido("tamanho", "O tamanho da página deve estar entre 1 e 100"));
        }
        String propriedade = NotificacaoSpecs.propriedadeDeOrdenacao(ordenarPor);
        if (propriedade == null) {
            return Result.err(parametroInvalido("ordenarPor",
                    "Ordenação inválida; use uma de: " + chavesOrdenacao()));
        }
        boolean ascendente = direcao.equalsIgnoreCase("asc");
        if (!ascendente && !direcao.equalsIgnoreCase("desc")) {
            return Result.err(parametroInvalido("direcao", "Direção inválida; use asc ou desc"));
        }
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return Result.err(parametroInvalido("dataFim", "A data final não pode ser anterior à data inicial"));
        }

        Specification<Notificacao> especificacao =
                NotificacaoSpecs.comFiltros(uf, municipio, tipo, sexo, dataInicio, dataFim);
        if (duplicadas) {
            especificacao = especificacao.and(NotificacaoSpecs.comAgravoDuplicado());
        }

        Sort.Direction sentido = ascendente ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Notificacao> resultados = repositorio.findAll(
                especificacao,
                PageRequest.of(pagina, tamanho, Sort.by(sentido, propriedade)));

        PaginaRespostaDTO<NotificacaoResponseDTO> envelope = new PaginaRespostaDTO<>(
                resultados.getContent().stream().map(NotificacaoMapper::paraResposta).toList(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages());
        return Result.ok(envelope);
    }


    private void aplicarRegrasDeEscrita(Notificacao notificacao) {
        notificacao.setAgravoDoenca(Textos.normalizar(notificacao.getAgravoDoenca()));

        Paciente paciente = notificacao.getPaciente();
        if (paciente == null) {
            return;
        }
        paciente.setNomePaciente(Textos.normalizar(paciente.getNomePaciente()));
        paciente.setNomeMae(Textos.normalizar(paciente.getNomeMae()));

        Endereco endereco = paciente.getEndereco();
        if (endereco != null && (endereco.getPaisResidencia() == null || endereco.getPaisResidencia().isBlank())) {
            endereco.setPaisResidencia("Brasil");
        }

        if (paciente.getSexo() == Sexo.FEMININO
                && paciente.getGestante() == null
                && idadeMenorQueSete(paciente)) {
            paciente.setGestante(PeriodoGestacional.NAO_SE_APLICA);
        }
    }

    private boolean idadeMenorQueSete(Paciente paciente) {
        Integer idade = paciente.getIdade();
        if (paciente.getDataNascimento() != null) {
            LocalDate hoje = LocalDate.now(relogio);
            if (paciente.getDataNascimento().isAfter(hoje)) {
                return false;
            }
            idade = Period.between(paciente.getDataNascimento(), hoje).getYears();
        }
        return idade != null && idade < 7;
    }

    private static ErroServico naoEncontrada(String numero) {
        return new ErroServico.NaoEncontrado("Notificação " + numero + " não encontrada");
    }

    private static ErroServico.ParametroInvalido parametroInvalido(String campo, String mensagem) {
        return new ErroServico.ParametroInvalido(List.of(new ViolacaoCampo(campo, mensagem)));
    }

    private static String chavesOrdenacao() {
        return NotificacaoSpecs.chavesDeOrdenacao().stream()
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
