package dev.pulsestack.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PulseStack – Auth Service")
                        .description("Registrierung und Login. Gibt JWT-Token zurück, der in allen anderen Services als Bearer-Token verwendet wird.")
                        .version("1.0.0"));
    }
}
