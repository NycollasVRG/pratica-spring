package com.pratica.notificacao.dto.request;

import com.pratica.notificacao.domain.enums.PeriodoGestacional;
import com.pratica.notificacao.domain.enums.RacaCor;
import com.pratica.notificacao.domain.enums.Sexo;
import com.pratica.notificacao.validation.RegrasPaciente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@RegrasPaciente
public record PacienteRequestDTO(
    @NotBlank(message = "O nome do paciente é obrigatório")
    String nomePaciente,

    @PastOrPresent(message = "A data de nascimento não pode ser no futuro")
    LocalDate dataNascimento,

    @Min(value = 0, message = "A idade não pode ser negativa")
    @Max(value = 150, message = "A idade deve ser no máximo 150 anos")
    Integer idade,
    
    @NotNull(message = "O sexo do paciente é obrigatório")
    Sexo sexo,
    
    PeriodoGestacional gestante,

    RacaCor racaCor,
    Integer escolaridade,

    @Pattern(regexp = "^(\\d{15})?$", message = "O número do Cartão SUS deve ter exatamente 15 dígitos")
    String numeroCartaoSus,

    String nomeMae,

    @Pattern(regexp = "^(\\d{10,11})?$", message = "O telefone deve ter 10 ou 11 dígitos")
    String telefone,
    
    @Valid
    @NotNull(message = "Os dados de residência são obrigatórios")
    EnderecoRequestDTO endereco
) {}
