package br.com.vidaplena.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PacienteRequest(@NotBlank String codigo, @NotBlank String cpf) {}
