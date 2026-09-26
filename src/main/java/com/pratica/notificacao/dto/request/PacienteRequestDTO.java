package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.RacaCor;
import com.pratica.notificacao.domain.enums.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PacienteRequestDTO(
    @NotBlank(message = "O nome do paciente é obrigatório")
    String nomePaciente,

    @PastOrPresent(message = "A data de nascimento não pode ser no futuro")
    LocalDate dataNascimento,
    Integer idade,
    
    @NotNull(message = "O sexo do paciente é obrigatório")
    Sexo sexo,
    
    @PastOrPresent(message = "A data de gestação não pode ser no futuro")
    Integer gestante,

    RacaCor racaCor,
    Integer escolaridade,
    String numeroCartaoSus,
    String nomeMae,
    String telefone,
    
    @Valid
    EnderecoRequestDTO endereco
) {}
