package br.com.vidaplena.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank @Size(min = 5, max = 120) String nome,
        @NotBlank @Pattern(regexp = "CRM-MA \\d{3,7}", message = "CRM inválido. Use o formato CRM-MA 0000") String crm,
        @NotBlank @Size(min = 10, max = 20) String telefone,
        @NotBlank @Email String email,
        // BCrypt só considera os primeiros 72 bytes
        @NotBlank @Size(min = 8, max = 72) String senha) {}
