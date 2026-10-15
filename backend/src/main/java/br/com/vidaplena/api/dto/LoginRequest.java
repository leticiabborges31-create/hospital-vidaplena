package br.com.vidaplena.api.dto;

import jakarta.validation.constraints.NotBlank;

// "email" aceita o e-mail do cadastro ou o usuário do administrador (ex.: admin).
public record LoginRequest(@NotBlank String email, @NotBlank String senha) {}
