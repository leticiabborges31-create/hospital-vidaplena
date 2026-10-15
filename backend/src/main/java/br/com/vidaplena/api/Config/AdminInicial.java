package br.com.vidaplena.api.Config;

import br.com.vidaplena.api.Model.Perfil;
import br.com.vidaplena.api.Model.Usuario;
import br.com.vidaplena.api.Repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Cria o administrador na primeira vez que a API sobe. A senha vai para o banco já com BCrypt. */
@Configuration
public class AdminInicial {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    @Bean
    public ApplicationRunner criarAdmin(UsuarioRepository repo, PasswordEncoder encoder,
                                        @Value("${app.admin.usuario}") String usuario,
                                        @Value("${app.admin.senha}") String senha) {
        return args -> {
            String login = usuario.trim().toLowerCase();
            if (repo.existsByEmail(login)) return;

            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setCrm("ADMIN");
            admin.setTelefone("-");
            admin.setEmail(login);
            admin.setSenhaHash(encoder.encode(senha));
            admin.setPerfil(Perfil.ADMIN);
            repo.save(admin);
            log.warn("Administrador '{}' criado. Troque a senha padrão (variável ADMIN_SENHA) antes de publicar o sistema.", login);
        };
    }
}
