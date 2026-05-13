package dev.pulsestack.processing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NewsItemRepository extends JpaRepository<NewsItemEntity, UUID> {

    boolean existsByExternalIdAndSource(
            String externalId,
            dev.pulsestack.domain.model.NewsSource source
    );

    List<NewsItemEntity> findByChannelIdOrderByFetchedAtDesc(UUID channelId);
}
