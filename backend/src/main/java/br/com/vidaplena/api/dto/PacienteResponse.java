package br.com.vidaplena.api.dto;

import br.com.vidaplena.api.Model.Agendamento;

import java.time.LocalDateTime;

/** Resposta enxuta para o paciente: sem CPF nem telefone. */
public record PacienteResponse(String codigo, String especialidade, String medico,
                               LocalDateTime dataHora, String status) {
    public static PacienteResponse de(Agendamento a) {
        return new PacienteResponse(a.getCodigo(), a.getEspecialidade(), a.getMedico(),
                a.getDataHora(), a.getStatus().name());
    }
}
