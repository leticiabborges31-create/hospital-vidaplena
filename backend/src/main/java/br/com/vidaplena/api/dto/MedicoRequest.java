package br.com.vidaplena.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MedicoRequest(@NotBlank String nome,
                            @NotBlank String crm,
                            @NotBlank String especialidade,
                            String telefone,
                            String email,
                            @Pattern(regexp = "Presencial|Teleconsulta|Ambos",
                                     message = "Tipo deve ser Presencial, Teleconsulta ou Ambos") String tipo) {}