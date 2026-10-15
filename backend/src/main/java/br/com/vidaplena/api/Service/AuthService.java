package br.com.vidaplena.api.service;

import br.com.vidaplena.api.Model.Perfil;
import br.com.vidaplena.api.Model.Usuario;
import br.com.vidaplena.api.Repository.UsuarioRepository;
import br.com.vidaplena.api.dto.CadastroRequest;
import br.com.vidaplena.api.dto.LoginRequest;
import br.com.vidaplena.api.dto.LoginResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    // Hash falso: o tempo de resposta não revela se o e-mail existe.
    private final String hashFalso;

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.hashFalso = encoder.encode("senha-inexistente");
    }

    public Usuario cadastrar(CadastroRequest r) {
        String email = r.email().trim().toLowerCase();
        if (repo.existsByEmail(email) || repo.existsByCrm(r.crm())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ou CRM já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(r.nome().trim());
        u.setCrm(r.crm());
        u.setTelefone(r.telefone());
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(r.senha()));
        u.setPerfil(Perfil.EQUIPE);   // só o admin inicial tem perfil ADMIN
        try {
            return repo.save(u);
        } catch (DataIntegrityViolationException e) {
            // dois cadastros iguais ao mesmo tempo
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ou CRM já cadastrado");
        }
    }

    public LoginResponse entrar(LoginRequest r) {
        Usuario u = repo.findByEmail(r.email().trim().toLowerCase()).orElse(null);
        boolean senhaOk = encoder.matches(r.senha(), u != null ? u.getSenhaHash() : hashFalso);
        if (u == null || !senhaOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");
        }
        return new LoginResponse(jwt.gerar(u), u.getNome(), u.getEmail(), u.getPerfil().name());
    }
}
