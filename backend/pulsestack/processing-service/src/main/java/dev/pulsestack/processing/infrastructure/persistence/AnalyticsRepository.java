package dev.pulsestack.processing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<NewsItemEntity, UUID> {

    @Query(value = """
            SELECT
                n.channel_id   AS channelId,
                c.display_name AS channelName,
                n.source       AS source,
                COUNT(*)       AS cnt
            FROM news_items n
            JOIN channels c ON c.id = n.channel_id
            WHERE n.fetched_at >= :since
            GROUP BY n.channel_id, c.display_name, n.source
            ORDER BY c.display_name, n.source
            """, nativeQuery = true)
    List<Object[]> countByChannelAndSource(@Param("since") Instant since);
}
