package com.pratica.notificacao.mapper;

import com.pratica.notificacao.domain.Endereco;
import com.pratica.notificacao.domain.Investigacao;
import com.pratica.notificacao.domain.Notificacao;
import com.pratica.notificacao.domain.Paciente;
import com.pratica.notificacao.dto.request.EnderecoRequestDTO;
import com.pratica.notificacao.dto.request.InvestigacaoRequestDTO;
import com.pratica.notificacao.dto.request.NotificacaoRequestDTO;
import com.pratica.notificacao.dto.request.PacienteRequestDTO;
import com.pratica.notificacao.dto.response.EnderecoResponseDTO;
import com.pratica.notificacao.dto.response.InvestigacaoResponseDTO;
import com.pratica.notificacao.dto.response.NotificacaoResponseDTO;
import com.pratica.notificacao.dto.response.PacienteResponseDTO;


public final class NotificacaoMapper {

    private NotificacaoMapper() {}

    public static Notificacao paraEntidade(NotificacaoRequestDTO dto) {
        Notificacao notificacao = new Notificacao();
        notificacao.setNumeroNotificacao(dto.numeroNotificacao());
        notificacao.setTipoNotificacao(dto.tipoNotificacao());
        notificacao.setAgravoDoenca(dto.agravoDoenca());
        notificacao.setDataNotificacao(dto.dataNotificacao());
        notificacao.setUfNotificacao(dto.ufNotificacao());
        notificacao.setMunicipioNotificacao(dto.municipioNotificacao());
        notificacao.setUnidadeSaudeNotificadora(dto.unidadeSaudeNotificadora());
        notificacao.setDataPrimeirosSintomas(dto.dataPrimeirosSintomas());
        notificacao.setPaciente(paraPaciente(dto.paciente()));
        notificacao.setInvestigacao(paraInvestigacao(dto.investigacao()));
        return notificacao;
    }

    public static NotificacaoResponseDTO paraResposta(Notificacao entidade) {
        return new NotificacaoResponseDTO(
                entidade.getNumeroNotificacao(),
                entidade.getTipoNotificacao(),
                entidade.getAgravoDoenca(),
                entidade.getDataNotificacao(),
                entidade.getUfNotificacao(),
                entidade.getMunicipioNotificacao(),
                entidade.getUnidadeSaudeNotificadora(),
                entidade.getDataPrimeirosSintomas(),
                paraResposta(entidade.getPaciente()),
                paraResposta(entidade.getInvestigacao()));
    }

    private static Paciente paraPaciente(PacienteRequestDTO dto) {
        if (dto == null) return null;
        Paciente paciente = new Paciente();
        paciente.setNomePaciente(dto.nomePaciente());
        paciente.setDataNascimento(dto.dataNascimento());
        paciente.setIdade(dto.idade());
        paciente.setSexo(dto.sexo());
        paciente.setGestante(dto.gestante());
        paciente.setRacaCor(dto.racaCor());
        paciente.setEscolaridade(dto.escolaridade());
        paciente.setNumeroCartaoSus(dto.numeroCartaoSus());
        paciente.setNomeMae(dto.nomeMae());
        paciente.setTelefone(dto.telefone());
        paciente.setEndereco(paraEndereco(dto.endereco()));
        return paciente;
    }

    private static Endereco paraEndereco(EnderecoRequestDTO dto) {
        if (dto == null) return null;
        Endereco endereco = new Endereco();
        endereco.setUfResidencia(dto.ufResidencia());
        endereco.setMunicipioResidencia(dto.municipioResidencia());
        endereco.setDistritoResidencia(dto.distritoResidencia());
        endereco.setBairroResidencia(dto.bairroResidencia());
        endereco.setLogradouroResidencia(dto.logradouroResidencia());
        endereco.setNumeroResidencia(dto.numeroResidencia());
        endereco.setComplementoResidencia(dto.complementoResidencia());
        endereco.setGeoCampo1(dto.geoCampo1());
        endereco.setGeoCampo2(dto.geoCampo2());
        endereco.setPontoReferenciaResidencia(dto.pontoReferenciaResidencia());
        endereco.setCepResidencia(dto.cepResidencia());
        endereco.setZonaResidencia(dto.zonaResidencia());
        endereco.setPaisResidencia(dto.paisResidencia());
        return endereco;
    }

    private static Investigacao paraInvestigacao(InvestigacaoRequestDTO dto) {
        if (dto == null) return null;
        Investigacao investigacao = new Investigacao();
        investigacao.setDataInvestigacao(dto.dataInvestigacao());
        investigacao.setClassificacaoFinal(dto.classificacaoFinal());
        investigacao.setCriterioConfirmacaoDescarte(dto.criterioConfirmacaoDescarte());
        investigacao.setAutoctoneMunicipioResidencia(dto.autoctoneMunicipioResidencia());
        investigacao.setUfLocalInfeccao(dto.ufLocalInfeccao());
        investigacao.setPaisLocalInfeccao(dto.paisLocalInfeccao());
        investigacao.setMunicipioLocalInfeccao(dto.municipioLocalInfeccao());
        investigacao.setDistritoLocalInfeccao(dto.distritoLocalInfeccao());
        investigacao.setBairroLocalInfeccao(dto.bairroLocalInfeccao());
        investigacao.setDoencaRelacionadaTrabalho(dto.doencaRelacionadaTrabalho());
        investigacao.setEvolucaoCaso(dto.evolucaoCaso());
        investigacao.setDataObito(dto.dataObito());
        investigacao.setDataEncerramento(dto.dataEncerramento());
        return investigacao;
    }

    private static PacienteResponseDTO paraResposta(Paciente entidade) {
        if (entidade == null) return null;
        return new PacienteResponseDTO(
                entidade.getNomePaciente(),
                entidade.getDataNascimento(),
                entidade.getIdade(),
                entidade.getSexo(),
                entidade.getGestante(),
                entidade.getRacaCor(),
                entidade.getEscolaridade(),
                entidade.getNumeroCartaoSus(),
                entidade.getNomeMae(),
                entidade.getTelefone(),
                paraResposta(entidade.getEndereco()));
    }

    private static EnderecoResponseDTO paraResposta(Endereco entidade) {
        if (entidade == null) return null;
        return new EnderecoResponseDTO(
                entidade.getUfResidencia(),
                entidade.getMunicipioResidencia(),
                entidade.getDistritoResidencia(),
                entidade.getBairroResidencia(),
                entidade.getLogradouroResidencia(),
                entidade.getNumeroResidencia(),
                entidade.getComplementoResidencia(),
                entidade.getGeoCampo1(),
                entidade.getGeoCampo2(),
                entidade.getPontoReferenciaResidencia(),
                entidade.getCepResidencia(),
                entidade.getZonaResidencia(),
                entidade.getPaisResidencia());
    }

    private static InvestigacaoResponseDTO paraResposta(Investigacao entidade) {
        if (entidade == null) return null;
        return new InvestigacaoResponseDTO(
                entidade.getDataInvestigacao(),
                entidade.getClassificacaoFinal(),
                entidade.getCriterioConfirmacaoDescarte(),
                entidade.getAutoctoneMunicipioResidencia(),
                entidade.getUfLocalInfeccao(),
                entidade.getPaisLocalInfeccao(),
                entidade.getMunicipioLocalInfeccao(),
                entidade.getDistritoLocalInfeccao(),
                entidade.getBairroLocalInfeccao(),
                entidade.getDoencaRelacionadaTrabalho(),
                entidade.getEvolucaoCaso(),
                entidade.getDataObito(),
                entidade.getDataEncerramento());
    }
}
