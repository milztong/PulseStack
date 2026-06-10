package dev.pulsestack.ingestion.domain.port;

import dev.pulsestack.domain.model.NewsItem;

/**
 * Ausgehender Port: Publisht neue News-Items als Events.
 * Implementiert durch KafkaNewsEventPublisher.
 */
public interface NewsEventPublisher {

    /**
     * Sendet ein neues News-Item als Event in den raw-news Topic.
     *
     * @param newsItem Das zu publizierende Item – niemals null
     */
    void publish(NewsItem newsItem);
}
