package dev.pulsestack.ingestion.infrastructure.persistence;

import dev.pulsestack.domain.model.NewsSource;
import jakarta.persistence.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "channels")
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column
    private String description;

    @Column(name = "external_only", nullable = false)
    private boolean externalOnly;

    /** Suchbegriff je Quelle. Fehlt ein Eintrag, gilt der Channel-Name. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "channel_source_queries",
            joinColumns = @JoinColumn(name = "channel_id")
    )
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "source")
    @Column(name = "search_query", nullable = false)
    private Map<NewsSource, String> sourceQueries = new EnumMap<>(NewsSource.class);

    protected ChannelEntity() {}

    public ChannelEntity(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isExternalOnly() { return externalOnly; }
    public Map<NewsSource, String> getSourceQueries() { return sourceQueries; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
}
