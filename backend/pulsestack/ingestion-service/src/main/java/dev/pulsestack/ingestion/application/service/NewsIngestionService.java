package dev.pulsestack.ingestion.application.service;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.ingestion.domain.port.DuplicateChecker;
import dev.pulsestack.ingestion.domain.port.NewsEventPublisher;
import dev.pulsestack.ingestion.domain.port.NewsSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NewsIngestionService {

    private static final Logger log = LoggerFactory.getLogger(NewsIngestionService.class);

    private static final int RICH_PREVIEW_LIMIT = 3;

    private final Map<NewsSource, NewsSourcePort> sourceAdapters;
    private final DuplicateChecker duplicateChecker;
    private final NewsEventPublisher eventPublisher;
    private final ChannelLoader channelLoader;

    // Konstruktor-Injection - kein @Autowired auf Feldern
    public NewsIngestionService(
            List<NewsSourcePort> adapters,
            DuplicateChecker duplicateChecker,
            NewsEventPublisher eventPublisher,
            ChannelLoader channelLoader
    ) {
        // Map: NewsSource -> Adapter (Open/Closed: neue Adapter werden automatisch registriert)
        this.sourceAdapters = adapters.stream()
                .collect(Collectors.toMap(NewsSourcePort::getSource, Function.identity()));
        this.duplicateChecker = duplicateChecker;
        this.eventPublisher = eventPublisher;
        this.channelLoader = channelLoader;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "PT8H")
    public void runIngestionForAllChannels() {
        log.info("Starting ingestion run for all channels and sources");
        List<Channel> channels = channelLoader.loadIngestable();

        channels.forEach(channel ->
                sourceAdapters.values().forEach(adapter ->
                        ingestForChannelAndSource(channel, adapter)
                )
        );

        log.info("Ingestion run complete. Processed {} channels x {} sources",
                channels.size(), sourceAdapters.size());
    }

    private void ingestForChannelAndSource(Channel channel, NewsSourcePort adapter) {
        try {
            List<NewsItem> items = adapter.fetchLatest(channel);
            log.debug("Fetched {} items from {} for channel '{}'",
                    items.size(), adapter.getSource(), channel.name());

            long published = publishNewItems(items, channel.name());
            log.info("Published {}/{} new items from {} for channel '{}'",
                    published, items.size(), adapter.getSource(), channel.name());

        } catch (Exception e) {
            // Einen fehlgeschlagenen Adapter nicht den ganzen Job stoppen lassen
            log.error("Ingestion failed for source={} channel={}: {}",
                    adapter.getSource(), channel.name(), e.getMessage(), e);
        }
    }

    private long publishNewItems(List<NewsItem> items, String channelName) {
        int previewCount = 0;

        long publishedCount = 0;
        for (NewsItem item : items) {
            if (duplicateChecker.isAlreadySeen(item.externalId())) {
                continue;
            }

            NewsItem itemToPublish = item;
            if (previewCount < RICH_PREVIEW_LIMIT && item.thumbnailUrl() != null) {
                previewCount++;
            }

            eventPublisher.publish(itemToPublish);
            publishedCount++;
        }
        return publishedCount;
    }
}
