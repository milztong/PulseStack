package dev.pulsestack.ingestion.infrastructure.persistence;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.ingestion.application.service.ChannelLoader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaChannelLoader implements ChannelLoader {

    private final ChannelRepository repository;

    public JpaChannelLoader(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Channel> loadAll() {
        return repository.findAll().stream()
                .map(e -> new Channel(e.getId(), e.getName(),
                        e.getDisplayName(), e.getDescription()))
                .toList();
    }
}
