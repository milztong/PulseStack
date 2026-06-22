package dev.pulsestack.processing.application.service;

import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.processing.infrastructure.persistence.NewsItemRepository;
import dev.pulsestack.processing.infrastructure.websocket.NewsWebSocketBroadcaster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsProcessingServiceTest {

    @Mock NewsItemRepository repository;
    @Mock NewsWebSocketBroadcaster broadcaster;

    @InjectMocks NewsProcessingService service;

    private static final NewsItem ITEM = NewsItem.of(
            "ext-123", NewsSource.REDDIT, UUID.randomUUID(),
            "Test Article", "https://reddit.com/test",
            "author1", 42, Instant.now()
    );

    @Test
    void process_newItem_savesAndBroadcasts() {
        when(repository.existsByExternalIdAndSource(ITEM.externalId(), ITEM.source()))
                .thenReturn(false);

        service.process(ITEM);

        verify(repository).save(any());
        verify(broadcaster).broadcast(ITEM);
    }

    @Test
    void process_duplicateItem_skipsAll() {
        when(repository.existsByExternalIdAndSource(ITEM.externalId(), ITEM.source()))
                .thenReturn(true);

        service.process(ITEM);

        verify(repository, never()).save(any());
        verify(broadcaster, never()).broadcast(any());
    }

    @Test
    void process_newItem_savesWithCorrectExternalId() {
        when(repository.existsByExternalIdAndSource(any(), any())).thenReturn(false);

        service.process(ITEM);

        ArgumentCaptor<dev.pulsestack.processing.infrastructure.persistence.NewsItemEntity> captor =
                ArgumentCaptor.forClass(dev.pulsestack.processing.infrastructure.persistence.NewsItemEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getExternalId()).isEqualTo("ext-123");
        assertThat(captor.getValue().getSource()).isEqualTo(NewsSource.REDDIT);
    }

    @Test
    void process_duplicateCheck_usesExternalIdAndSource() {
        when(repository.existsByExternalIdAndSource("ext-123", NewsSource.REDDIT))
                .thenReturn(false);

        service.process(ITEM);

        verify(repository).existsByExternalIdAndSource("ext-123", NewsSource.REDDIT);
    }
}
