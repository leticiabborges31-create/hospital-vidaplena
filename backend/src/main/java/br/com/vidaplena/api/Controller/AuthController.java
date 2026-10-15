package br.com.vidaplena.api.controller;

import br.com.vidaplena.api.dto.CadastroRequest;
import br.com.vidaplena.api.dto.LoginRequest;
import br.com.vidaplena.api.dto.LoginResponse;
import br.com.vidaplena.api.dto.UsuarioResponse;
import br.com.vidaplena.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        return UsuarioResponse.de(service.cadastrar(req));
    }

    @PostMapping("/login")
    public LoginResponse entrar(@Valid @RequestBody LoginRequest req) {
        return service.entrar(req);
    }
}
