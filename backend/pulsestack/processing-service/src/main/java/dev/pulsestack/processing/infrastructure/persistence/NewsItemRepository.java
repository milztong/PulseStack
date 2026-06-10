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

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT n FROM NewsItemEntity n
            WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY n.fetchedAt DESC
            """)
    List<NewsItemEntity> searchByTitle(
            @org.springframework.data.repository.query.Param("query") String query,
            org.springframework.data.domain.Pageable pageable
    );
}
