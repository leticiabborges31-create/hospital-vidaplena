package br.com.vidaplena.api.dto;

import br.com.vidaplena.api.Model.Usuario;

public record UsuarioResponse(String nome, String crm, String email) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getNome(), u.getCrm(), u.getEmail());
    }
}
