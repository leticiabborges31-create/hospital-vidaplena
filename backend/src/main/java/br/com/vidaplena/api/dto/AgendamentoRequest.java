package br.com.vidaplena.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AgendamentoRequest(@NotBlank @Size(min = 5, max = 120) String nomePaciente,
                                 @NotBlank @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF inválido") String cpf,
                                 @NotBlank @Size(min = 10, max = 20) String telefone,
                                 @Size(max = 60) String convenio,
                                 @NotBlank @Size(max = 80) String especialidade,
                                 @NotBlank @Size(max = 120) String medico,
                                 @NotBlank @Pattern(regexp = "Presencial|Teleconsulta", message = "Tipo inválido") String tipo,
                                 LocalDateTime dataHora,
                                 Boolean espera) {

    /** O campo é opcional no JSON: ausente significa agendamento normal. */
    public boolean ehEspera() {
        return Boolean.TRUE.equals(espera);
    }
}
