package dev.pulsestack.ingestion.infrastructure.persistence;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.ingestion.application.service.ChannelLoader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Temporärer Stub – Phase 1.
 * TODO Phase 2: Durch echten JPA Repository-Loader ersetzen.
 */
@Component
public class StubChannelLoader implements ChannelLoader {

    @Override
    public List<Channel> loadAll() {
        return List.of(
                new Channel(UUID.randomUUID(), "java", "Java", "Java ecosystem"),
                new Channel(UUID.randomUUID(), "ai", "AI & ML", "Machine learning")
        );
    }
}
