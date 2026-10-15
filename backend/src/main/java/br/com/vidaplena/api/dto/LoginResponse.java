package br.com.vidaplena.api.dto;

public record LoginResponse(String token, String nome, String email, String perfil) {}
