package dev.pulsestack.ingestion.domain.port;

import dev.pulsestack.domain.model.NewsItem;

public interface NewsEventPublisher {

    void publish(NewsItem newsItem);
}
