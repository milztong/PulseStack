package dev.pulsestack.processing.infrastructure.persistence;

import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "news_items")
public class NewsItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsSource source;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(name = "thumbnail_url", length = 2000)
    private String thumbnailUrl;

    @Column
    private String author;

    @Column
    private Integer score;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    protected NewsItemEntity() {}

    public static NewsItemEntity fromDomain(NewsItem item) {
        NewsItemEntity entity = new NewsItemEntity();
        entity.externalId = item.externalId();
        entity.source = item.source();
        entity.channelId = item.channelId();
        entity.title = item.title();
        entity.url = item.url();
        entity.thumbnailUrl = item.thumbnailUrl();
        entity.author = item.author();
        entity.score = item.score();
        entity.publishedAt = item.publishedAt();
        entity.fetchedAt = item.fetchedAt();
        return entity;
    }

    public UUID getId() { return id; }
    public String getExternalId() { return externalId; }
    public NewsSource getSource() { return source; }
    public UUID getChannelId() { return channelId; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getAuthor() { return author; }
    public Integer getScore() { return score; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getFetchedAt() { return fetchedAt; }
}
