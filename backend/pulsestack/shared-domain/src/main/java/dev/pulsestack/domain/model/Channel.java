package dev.pulsestack.domain.model;

import java.util.Map;
import java.util.UUID;

public record Channel(
        UUID id,
        String name,
        String displayName,
        String description,
        Map<NewsSource, String> sourceQueries
) {
    public Channel {
        sourceQueries = sourceQueries == null ? Map.of() : Map.copyOf(sourceQueries);
    }

    public Channel(UUID id, String name, String displayName, String description) {
        this(id, name, displayName, description, Map.of());
    }

    /**
     * Der Suchbegriff, den dieser Channel bei einer Quelle verwendet.
     * Ohne Override wird der Channel-Name benutzt (z. B. "java" -> /r/java).
     */
    public String queryFor(NewsSource source) {
        return sourceQueries.getOrDefault(source, name);
    }
}
