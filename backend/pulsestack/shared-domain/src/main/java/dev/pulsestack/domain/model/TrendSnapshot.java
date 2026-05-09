package dev.pulsestack.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrendSnapshot(
        UUID channelId,
        NewsSource source,
        Instant snapshotAt,
        List<NewsItem> items,
        int totalCount
) {}
