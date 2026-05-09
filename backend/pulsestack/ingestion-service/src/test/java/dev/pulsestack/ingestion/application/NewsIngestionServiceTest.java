package dev.pulsestack.ingestion.application;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.ingestion.application.service.ChannelLoader;
import dev.pulsestack.ingestion.application.service.NewsIngestionService;
import dev.pulsestack.ingestion.domain.port.DuplicateChecker;
import dev.pulsestack.ingestion.domain.port.NewsEventPublisher;
import dev.pulsestack.ingestion.domain.port.NewsSourcePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests fuer NewsIngestionService.
 *
 * Wichtig: Kein Spring-Context, kein Kafka, kein Redis.
 * Alles wird gemockt – testet NUR die Business-Logik.
 *
 * Naming-Convention: should_[erwartetes Ergebnis]_when_[Bedingung]
 */
@ExtendWith(MockitoExtension.class)
class NewsIngestionServiceTest {

    @Mock
    private NewsSourcePort mockAdapter;

    @Mock
    private DuplicateChecker duplicateChecker;

    @Mock
    private NewsEventPublisher eventPublisher;

    @Mock
    private ChannelLoader channelLoader;

    private NewsIngestionService sut; // System Under Test

    private static final Channel TEST_CHANNEL = new Channel(
            UUID.randomUUID(), "java", "Java", "Java ecosystem"
    );

    private static final NewsItem TEST_ITEM = NewsItem.of(
            "reddit_abc123", NewsSource.REDDIT, TEST_CHANNEL.id(),
            "Spring Boot 3.3 released", "https://reddit.com/r/java/abc123",
            "user123", 500, Instant.now()
    );

    @BeforeEach
    void setUp() {
        when(mockAdapter.getSource()).thenReturn(NewsSource.REDDIT);
        sut = new NewsIngestionService(
                List.of(mockAdapter), duplicateChecker, eventPublisher, channelLoader
        );
    }

    @Test
    @DisplayName("should publish new item when not seen before")
    void should_publishItem_when_itemIsNew() {
        when(channelLoader.loadAll()).thenReturn(List.of(TEST_CHANNEL));
        when(mockAdapter.fetchLatest(TEST_CHANNEL)).thenReturn(List.of(TEST_ITEM));
        when(duplicateChecker.isAlreadySeen(TEST_ITEM.externalId())).thenReturn(false);

        sut.runIngestionForAllChannels();

        verify(eventPublisher, times(1)).publish(TEST_ITEM);
    }

    @Test
    @DisplayName("should skip item when already seen (duplicate)")
    void should_skipItem_when_itemIsDuplicate() {
        when(channelLoader.loadAll()).thenReturn(List.of(TEST_CHANNEL));
        when(mockAdapter.fetchLatest(TEST_CHANNEL)).thenReturn(List.of(TEST_ITEM));
        when(duplicateChecker.isAlreadySeen(TEST_ITEM.externalId())).thenReturn(true);

        sut.runIngestionForAllChannels();

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should continue processing other channels when one adapter fails")
    void should_continueProcessing_when_adapterThrowsException() {
        Channel failingChannel = new Channel(UUID.randomUUID(), "ai", "AI", "AI news");
        when(channelLoader.loadAll()).thenReturn(List.of(TEST_CHANNEL, failingChannel));
        when(mockAdapter.fetchLatest(TEST_CHANNEL)).thenReturn(List.of(TEST_ITEM));
        when(mockAdapter.fetchLatest(failingChannel)).thenThrow(new RuntimeException("API timeout"));
        when(duplicateChecker.isAlreadySeen(anyString())).thenReturn(false);

        // Kein Exception wird nach oben propagiert
        sut.runIngestionForAllChannels();

        // Das erste Channel-Item wurde trotzdem published
        verify(eventPublisher, times(1)).publish(TEST_ITEM);
    }
}
