package dev.pulsestack.ingestion.config;

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
                        .title("PulseStack – Ingestion Service")
                        .description("Pollt externe APIs (Reddit, YouTube, GitHub, NewsAPI) und veröffentlicht Events nach Kafka.")
                        .version("1.0.0"));
    }
}
