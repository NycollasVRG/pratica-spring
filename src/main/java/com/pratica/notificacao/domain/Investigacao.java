package com.pratica.notificacao.domain;

import com.pratica.notificacao.domain.enums.CriterioConfirmacao;
import com.pratica.notificacao.domain.enums.EvolucaoCaso;
import com.pratica.notificacao.domain.enums.SimNaoIgnorado;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
public class Investigacao {

    private LocalDate dataInvestigacao;
    private Integer classificacaoFinal;
    
    
    private CriterioConfirmacao criterioConfirmacaoDescarte;
    
    
    private SimNaoIgnorado autoctoneMunicipioResidencia;
    
    private String ufLocalInfeccao;
    private String paisLocalInfeccao;
    private String municipioLocalInfeccao;
    private String distritoLocalInfeccao;
    private String bairroLocalInfeccao;
    
    
    private SimNaoIgnorado doencaRelacionadaTrabalho;
    
    
    private EvolucaoCaso evolucaoCaso;
    
    private LocalDate dataObito;
    private LocalDate dataEncerramento;

    // Construtores, Getters e Setters
    public Investigacao() {}
    
    public LocalDate getDataInvestigacao() { return dataInvestigacao; }
    public void setDataInvestigacao(LocalDate dataInvestigacao) { this.dataInvestigacao = dataInvestigacao; }
    
    public Integer getClassificacaoFinal() { return classificacaoFinal; }
    public void setClassificacaoFinal(Integer classificacaoFinal) { this.classificacaoFinal = classificacaoFinal; }
    
    public CriterioConfirmacao getCriterioConfirmacaoDescarte() { return criterioConfirmacaoDescarte; }
    public void setCriterioConfirmacaoDescarte(CriterioConfirmacao criterioConfirmacaoDescarte) { this.criterioConfirmacaoDescarte = criterioConfirmacaoDescarte; }
    
    public SimNaoIgnorado getAutoctoneMunicipioResidencia() { return autoctoneMunicipioResidencia; }
    public void setAutoctoneMunicipioResidencia(SimNaoIgnorado autoctoneMunicipioResidencia) { this.autoctoneMunicipioResidencia = autoctoneMunicipioResidencia; }
    
    public String getUfLocalInfeccao() { return ufLocalInfeccao; }
    public void setUfLocalInfeccao(String ufLocalInfeccao) { this.ufLocalInfeccao = ufLocalInfeccao; }
    
    public String getPaisLocalInfeccao() { return paisLocalInfeccao; }
    public void setPaisLocalInfeccao(String paisLocalInfeccao) { this.paisLocalInfeccao = paisLocalInfeccao; }
    
    public String getMunicipioLocalInfeccao() { return municipioLocalInfeccao; }
    public void setMunicipioLocalInfeccao(String municipioLocalInfeccao) { this.municipioLocalInfeccao = municipioLocalInfeccao; }
    
    public String getDistritoLocalInfeccao() { return distritoLocalInfeccao; }
    public void setDistritoLocalInfeccao(String distritoLocalInfeccao) { this.distritoLocalInfeccao = distritoLocalInfeccao; }
    
    public String getBairroLocalInfeccao() { return bairroLocalInfeccao; }
    public void setBairroLocalInfeccao(String bairroLocalInfeccao) { this.bairroLocalInfeccao = bairroLocalInfeccao; }
    
    public SimNaoIgnorado getDoencaRelacionadaTrabalho() { return doencaRelacionadaTrabalho; }
    public void setDoencaRelacionadaTrabalho(SimNaoIgnorado doencaRelacionadaTrabalho) { this.doencaRelacionadaTrabalho = doencaRelacionadaTrabalho; }
    
    public EvolucaoCaso getEvolucaoCaso() { return evolucaoCaso; }
    public void setEvolucaoCaso(EvolucaoCaso evolucaoCaso) { this.evolucaoCaso = evolucaoCaso; }
    
    public LocalDate getDataObito() { return dataObito; }
    public void setDataObito(LocalDate dataObito) { this.dataObito = dataObito; }
    
    public LocalDate getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDate dataEncerramento) { this.dataEncerramento = dataEncerramento; }
}
