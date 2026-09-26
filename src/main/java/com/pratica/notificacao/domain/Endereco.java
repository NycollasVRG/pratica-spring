package com.pratica.notificacao.domain;

import com.pratica.notificacao.domain.enums.ZonaResidencia;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Endereco {
    
    private String ufResidencia;
    private String municipioResidencia;
    private String distritoResidencia;
    private String bairroResidencia;
    private String logradouroResidencia;
    private String numeroResidencia;
    private String complementoResidencia;
    
    private String geoCampo1;
    private String geoCampo2;
    private String pontoReferenciaResidencia;
    private String cepResidencia;
    
    
    private ZonaResidencia zonaResidencia;
    
    private String paisResidencia;

    // Construtores, Getters e Setters
    public Endereco() {}
    
    public String getUfResidencia() { return ufResidencia; }
    public void setUfResidencia(String ufResidencia) { this.ufResidencia = ufResidencia; }
    
    public String getMunicipioResidencia() { return municipioResidencia; }
    public void setMunicipioResidencia(String municipioResidencia) { this.municipioResidencia = municipioResidencia; }
    
    public String getDistritoResidencia() { return distritoResidencia; }
    public void setDistritoResidencia(String distritoResidencia) { this.distritoResidencia = distritoResidencia; }
    
    public String getBairroResidencia() { return bairroResidencia; }
    public void setBairroResidencia(String bairroResidencia) { this.bairroResidencia = bairroResidencia; }
    
    public String getLogradouroResidencia() { return logradouroResidencia; }
    public void setLogradouroResidencia(String logradouroResidencia) { this.logradouroResidencia = logradouroResidencia; }
    
    public String getNumeroResidencia() { return numeroResidencia; }
    public void setNumeroResidencia(String numeroResidencia) { this.numeroResidencia = numeroResidencia; }
    
    public String getComplementoResidencia() { return complementoResidencia; }
    public void setComplementoResidencia(String complementoResidencia) { this.complementoResidencia = complementoResidencia; }
    
    public String getGeoCampo1() { return geoCampo1; }
    public void setGeoCampo1(String geoCampo1) { this.geoCampo1 = geoCampo1; }
    
    public String getGeoCampo2() { return geoCampo2; }
    public void setGeoCampo2(String geoCampo2) { this.geoCampo2 = geoCampo2; }
    
    public String getPontoReferenciaResidencia() { return pontoReferenciaResidencia; }
    public void setPontoReferenciaResidencia(String pontoReferenciaResidencia) { this.pontoReferenciaResidencia = pontoReferenciaResidencia; }
    
    public String getCepResidencia() { return cepResidencia; }
    public void setCepResidencia(String cepResidencia) { this.cepResidencia = cepResidencia; }
    
    public ZonaResidencia getZonaResidencia() { return zonaResidencia; }
    public void setZonaResidencia(ZonaResidencia zonaResidencia) { this.zonaResidencia = zonaResidencia; }
    
    public String getPaisResidencia() { return paisResidencia; }
    public void setPaisResidencia(String paisResidencia) { this.paisResidencia = paisResidencia; }
}
