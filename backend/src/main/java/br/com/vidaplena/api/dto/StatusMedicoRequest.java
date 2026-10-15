package br.com.vidaplena.api.dto;

import br.com.vidaplena.api.Model.StatusMedico;
import jakarta.validation.constraints.NotNull;

public record StatusMedicoRequest(@NotNull StatusMedico status) {}