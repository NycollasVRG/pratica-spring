package com.pratica.notificacao.dto.response;

import com.pratica.notificacao.domain.enums.RacaCor;
import com.pratica.notificacao.domain.enums.Sexo;
import java.time.LocalDate;

public record PacienteResponseDTO(
    String nomePaciente,
    LocalDate dataNascimento,
    Integer idade,
    Sexo sexo,
    Integer gestante,
    RacaCor racaCor,
    Integer escolaridade,
    String numeroCartaoSus,
    String nomeMae,
    String telefone,
    EnderecoResponseDTO endereco
) {}
