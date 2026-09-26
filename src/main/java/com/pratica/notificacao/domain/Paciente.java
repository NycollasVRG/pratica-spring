package com.pratica.notificacao.domain;

import com.pratica.notificacao.domain.enums.RacaCor;
import com.pratica.notificacao.domain.enums.Sexo;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
public class Paciente {
    
    private String nomePaciente;
    private LocalDate dataNascimento;
    private Integer idade;
    
    
    private Sexo sexo;
    
    private Integer gestante;
    
    
    private RacaCor racaCor;
    
    private Integer escolaridade;
    private String numeroCartaoSus;
    private String nomeMae;
    private String telefone;
    
    @Embedded
    private Endereco endereco;

    // Construtores, Getters e Setters
    public Paciente() {}

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }
    
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    
    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }
    
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    
    public Integer getGestante() { return gestante; }
    public void setGestante(Integer gestante) { this.gestante = gestante; }
    
    public RacaCor getRacaCor() { return racaCor; }
    public void setRacaCor(RacaCor racaCor) { this.racaCor = racaCor; }
    
    public Integer getEscolaridade() { return escolaridade; }
    public void setEscolaridade(Integer escolaridade) { this.escolaridade = escolaridade; }
    
    public String getNumeroCartaoSus() { return numeroCartaoSus; }
    public void setNumeroCartaoSus(String numeroCartaoSus) { this.numeroCartaoSus = numeroCartaoSus; }
    
    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }
    
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
}
