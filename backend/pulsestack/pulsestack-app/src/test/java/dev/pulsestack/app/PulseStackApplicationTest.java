package dev.pulsestack.app;

import dev.pulsestack.auth.api.AuthController;
import dev.pulsestack.chat.api.ChatController;
import dev.pulsestack.ingestion.api.ChannelController;
import dev.pulsestack.ingestion.application.service.NewsIngestionService;
import dev.pulsestack.ingestion.infrastructure.kafka.KafkaNewsEventPublisher;
import dev.pulsestack.processing.api.NewsItemController;
import dev.pulsestack.processing.infrastructure.kafka.NewsEventConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pulsestack;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "pulsestack.jwt.secret=test-secret-key-must-be-at-least-32-chars"
})
class PulseStackApplicationTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private NewsIngestionService newsIngestionService;

    @Test
    void loadsAllModulesInOneApplicationContext() {
        assertThat(context.getBean(AuthController.class)).isNotNull();
        assertThat(context.getBean(ChannelController.class)).isNotNull();
        assertThat(context.getBean(NewsItemController.class)).isNotNull();
        assertThat(context.getBean(ChatController.class)).isNotNull();
        assertThat(context.getBean(KafkaNewsEventPublisher.class)).isNotNull();
        assertThat(context.getBean(NewsEventConsumer.class)).isNotNull();

        assertThat(context.getBeansOfType(SecurityFilterChain.class)).hasSize(1);
        assertThat(context.getBeansOfType(WebSocketMessageBrokerConfigurer.class))
                .containsKey("webSocketConfig")
                .doesNotContainKeys("webSocketSecurityConfig");
        assertThat(context.getBeansOfType(WebSocketMessageBrokerConfigurer.class).entrySet())
                .filteredOn(entry -> entry.getValue().getClass().getName().startsWith("dev.pulsestack"))
                .hasSize(1);
    }
}
