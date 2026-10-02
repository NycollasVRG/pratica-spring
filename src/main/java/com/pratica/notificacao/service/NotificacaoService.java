package com.pratica.notificacao.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pratica.notificacao.common.Textos;
import com.pratica.notificacao.domain.Endereco;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.dto.request.NotificacaoFilterDTO;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PaginaRespostaDTO;
import com.pratica.notificacao.exception.ErroServico;
import com.pratica.notificacao.exception.ViolacaoCampo;
import com.pratica.notificacao.mapper.NotificacaoMapper;
import com.pratica.notificacao.repository.NotificacaoRepository;
import com.pratica.notificacao.specification.NotificacaoSpecs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repositorio;
    private final NotificacaoMapper mapper;
    private final Clock relogio;

    public NotificacaoService(NotificacaoRepository repositorio, NotificacaoMapper mapper, Clock relogio) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.relogio = relogio;
    }

    @Transactional
    public NotificacaoResponseDTO criar(NotificacaoRequestDTO dto) {
        if (repositorio.existsById(dto.numeroNotificacao())) {
            throw new ErroServico.Conflito("Já existe uma notificação com o número " + dto.numeroNotificacao());
        }
        Notificacao entidade = mapper.paraEntidade(dto);
        aplicarRegrasDeEscrita(entidade);
        repositorio.save(entidade);
        return mapper.paraResposta(entidade);
    }

    @Transactional(readOnly = true)
    public NotificacaoResponseDTO obter(String numero) {
        return repositorio.findById(numero)
                .map(mapper::paraResposta)
                .orElseThrow(() -> naoEncontrada(numero));
    }

    @Transactional
    public NotificacaoResponseDTO atualizar(String numero, NotificacaoRequestDTO dto) {
        if (!numero.equals(dto.numeroNotificacao())) {
            throw parametroInvalido("numeroNotificacao", "O número do corpo da requisição difere do número da URL");
        }
        
        Notificacao entidade = repositorio.findById(numero)
                .orElseThrow(() -> naoEncontrada(numero));
                
        mapper.atualizarEntidade(dto, entidade);
        aplicarRegrasDeEscrita(entidade);
        // Não é necessário chamar save explicitly pois a entidade está em estado managed, 
        // mas chamamos para deixar explícito
        repositorio.save(entidade);
        return mapper.paraResposta(entidade);
    }

    @Transactional
    public void remover(String numero) {
        if (!repositorio.existsById(numero)) {
            throw naoEncontrada(numero);
        }
        repositorio.deleteById(numero);
    }

    @Transactional(readOnly = true)
    public PaginaRespostaDTO<NotificacaoResponseDTO> listar(NotificacaoFilterDTO filtro, Pageable pageable) {

        if (filtro.dataInicio() != null && filtro.dataFim() != null && filtro.dataInicio().isAfter(filtro.dataFim())) {
            throw parametroInvalido("dataFim", "A data final não pode ser anterior à data inicial");
        }

        Specification<Notificacao> especificacao =
                NotificacaoSpecs.comFiltros(filtro.uf(), filtro.municipio(), filtro.tipo(), filtro.sexo(), filtro.dataInicio(), filtro.dataFim());
                
        if (filtro.duplicadas()) {
            especificacao = especificacao.and(NotificacaoSpecs.comAgravoDuplicado());
        }

        Page<Notificacao> resultados = repositorio.findAll(especificacao, pageable);

        return new PaginaRespostaDTO<>(
                resultados.getContent().stream().map(mapper::paraResposta).toList(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages());
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
}
