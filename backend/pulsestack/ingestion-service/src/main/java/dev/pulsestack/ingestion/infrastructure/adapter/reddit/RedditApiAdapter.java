package dev.pulsestack.ingestion.infrastructure.adapter.reddit;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.ingestion.domain.port.NewsSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Reddit API Adapter – Phase 1: Stub-Implementierung.
 *
 * Gibt Test-Daten zurueck damit die gesamte Pipeline (Kafka, Redis, Processing)
 * schon in Phase 1 getestet werden kann, ohne echten API-Key.
 *
 * TODO Phase 2: Echte Reddit-API Anbindung via WebClient + OAuth2.
 */
@Component
public class RedditApiAdapter implements NewsSourcePort {

    private static final Logger log = LoggerFactory.getLogger(RedditApiAdapter.class);

    @Override
    public List<NewsItem> fetchLatest(Channel channel) {
        log.info("Fetching from Reddit (STUB) for channel: {}", channel.name());

        // Stub: gibt ein Test-Item zurueck
        NewsItem stubItem = NewsItem.of(
                "stub_reddit_" + channel.name() + "_" + System.currentTimeMillis(),
                NewsSource.REDDIT,
                channel.id(),
                "[STUB] Top post in r/" + channel.name(),
                "https://reddit.com/r/" + channel.name(),
                "stub_user",
                42,
                Instant.now()
        );

        return List.of(stubItem);
    }

    @Override
    public NewsSource getSource() {
        return NewsSource.REDDIT;
    }
}
