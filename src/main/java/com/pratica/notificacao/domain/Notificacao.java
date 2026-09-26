package com.pratica.notificacao.domain;

import com.pratica.notificacao.domain.enums.TipoNotificacao;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    private String numeroNotificacao;
    
    
    private TipoNotificacao tipoNotificacao;
    
    private String agravoDoenca;
    private LocalDate dataNotificacao;
    
    private String ufNotificacao;
    private String municipioNotificacao;
    private String unidadeSaudeNotificadora;
    private LocalDate dataPrimeirosSintomas;
    
    @Embedded
    private Paciente paciente;
    
    @Embedded
    private Investigacao investigacao;

    // Construtores, Getters e Setters
    public Notificacao() {}
    
    public String getNumeroNotificacao() { return numeroNotificacao; }
    public void setNumeroNotificacao(String numeroNotificacao) { this.numeroNotificacao = numeroNotificacao; }
    
    public TipoNotificacao getTipoNotificacao() { return tipoNotificacao; }
    public void setTipoNotificacao(TipoNotificacao tipoNotificacao) { this.tipoNotificacao = tipoNotificacao; }
    
    public String getAgravoDoenca() { return agravoDoenca; }
    public void setAgravoDoenca(String agravoDoenca) { this.agravoDoenca = agravoDoenca; }
    
    public LocalDate getDataNotificacao() { return dataNotificacao; }
    public void setDataNotificacao(LocalDate dataNotificacao) { this.dataNotificacao = dataNotificacao; }
    
    public String getUfNotificacao() { return ufNotificacao; }
    public void setUfNotificacao(String ufNotificacao) { this.ufNotificacao = ufNotificacao; }
    
    public String getMunicipioNotificacao() { return municipioNotificacao; }
    public void setMunicipioNotificacao(String municipioNotificacao) { this.municipioNotificacao = municipioNotificacao; }
    
    public String getUnidadeSaudeNotificadora() { return unidadeSaudeNotificadora; }
    public void setUnidadeSaudeNotificadora(String unidadeSaudeNotificadora) { this.unidadeSaudeNotificadora = unidadeSaudeNotificadora; }
    
    public LocalDate getDataPrimeirosSintomas() { return dataPrimeirosSintomas; }
    public void setDataPrimeirosSintomas(LocalDate dataPrimeirosSintomas) { this.dataPrimeirosSintomas = dataPrimeirosSintomas; }
    
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    
    public Investigacao getInvestigacao() { return investigacao; }
    public void setInvestigacao(Investigacao investigacao) { this.investigacao = investigacao; }
}
