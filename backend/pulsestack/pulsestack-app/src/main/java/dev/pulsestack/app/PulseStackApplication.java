package dev.pulsestack.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "dev.pulsestack")
@EnableJpaRepositories(basePackages = "dev.pulsestack")
@ComponentScan(
        basePackages = "dev.pulsestack",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "dev\\.pulsestack\\..*ServiceApplication"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "dev\\.pulsestack\\.(auth|processing|chat)\\.config\\.SecurityConfig"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "dev\\.pulsestack\\.(auth|ingestion|processing)\\.config\\.OpenApiConfig"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "dev\\.pulsestack\\.ingestion\\.config\\.WebConfig"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "dev\\.pulsestack\\.processing\\.config\\.(WebSocketConfig|WebSocketSecurityConfig)"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "dev\\.pulsestack\\.chat\\.security\\.JwtAuthFilter"
                )
        }
)
public class PulseStackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulseStackApplication.class, args);
    }
}
