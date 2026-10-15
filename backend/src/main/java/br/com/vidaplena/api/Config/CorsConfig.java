package br.com.vidaplena.api.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.origem}")
    private String[] origens;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // quer dizer que qualquer rota da Api vai aceitar essas configurações de CORS.
                .allowedOrigins(origens)// (endereços vêm de app.cors.origem / variável CORS_ORIGEM, separados por vírgula) esta dizendo que sos esse endereço pode acessar a Api, se fosse * qualquer endereço poderia acessar a Api.
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // esta dizendo que esses metodos de criar, ler eetc podem ser usados na api.
                .allowedHeaders("*"); //informações sobre a carta exemplo content-type, authorization, etc e tbm é importante saber que quando a pessoa ja esta logada ela usa o authorization para enviar o token.
    }
}