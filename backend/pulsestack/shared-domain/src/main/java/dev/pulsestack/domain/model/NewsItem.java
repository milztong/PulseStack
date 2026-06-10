package dev.pulsestack.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Zentrales Domain-Objekt fuer alle News-Inhalte.
 * Kein Spring, keine JPA-Annotations - reines Java.
 * JPA-Mapping passiert im infrastructure-Layer (NewsItemEntity).
 */
public record NewsItem(
        UUID id,
        String externalId,
        NewsSource source,
        UUID channelId,
        String title,
        String url,
        String thumbnailUrl,
        String author,
        Integer score,
        Instant publishedAt,
        Instant fetchedAt
) {
    public static NewsItem of(
            String externalId,
            NewsSource source,
            UUID channelId,
            String title,
            String url,
            String author,
            Integer score,
            Instant publishedAt
    ) {
        return new NewsItem(null, externalId, source, channelId, title,
                url, null, author, score, publishedAt, Instant.now());
    }

    public NewsItem withThumbnail(String thumbnailUrl) {
        return new NewsItem(id, externalId, source, channelId, title,
                url, thumbnailUrl, author, score, publishedAt, fetchedAt);
    }
}
