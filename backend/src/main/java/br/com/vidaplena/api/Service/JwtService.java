package br.com.vidaplena.api.service;

import br.com.vidaplena.api.Model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final long horas;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiracao-horas}") long horas) {
        this.encoder = encoder;
        this.horas = horas;
    }

    public String gerar(Usuario u) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("vidaplena")
                .subject(u.getEmail())
                .claim("nome", u.getNome())
                .claim("perfil", u.getPerfil().name())
                .issuedAt(agora)
                .expiresAt(agora.plus(horas, ChronoUnit.HOURS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
