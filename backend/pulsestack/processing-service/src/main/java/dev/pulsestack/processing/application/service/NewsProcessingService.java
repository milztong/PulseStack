package dev.pulsestack.processing.application.service;

import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.processing.infrastructure.persistence.NewsItemEntity;
import dev.pulsestack.processing.infrastructure.persistence.NewsItemRepository;
import dev.pulsestack.processing.infrastructure.websocket.NewsWebSocketBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use-Case: Verarbeitet ein eingehendes News-Event.
 * 1. Duplikat-Check gegen PostgreSQL
 * 2. Speichern
 * 3. WebSocket Broadcast
 */
@Service
public class NewsProcessingService {

    private static final Logger log = LoggerFactory.getLogger(NewsProcessingService.class);

    private final NewsItemRepository repository;
    private final NewsWebSocketBroadcaster broadcaster;

    public NewsProcessingService(
            NewsItemRepository repository,
            NewsWebSocketBroadcaster broadcaster
    ) {
        this.repository = repository;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public void process(NewsItem newsItem) {
        if (repository.existsByExternalIdAndSource(newsItem.externalId(), newsItem.source())) {
            log.debug("Skipping duplicate: externalId={}", newsItem.externalId());
            return;
        }

        NewsItemEntity entity = NewsItemEntity.fromDomain(newsItem);
        repository.save(entity);
        log.info("Saved NewsItem: externalId={} source={} channel={}",
                newsItem.externalId(), newsItem.source(), newsItem.channelId());

        broadcaster.broadcast(newsItem);
    }
}
