package dev.pulsestack.ingestion.infrastructure.adapter.youtube;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.ingestion.domain.port.NewsSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "pulsestack.youtube.api-key")
public class YouTubeApiAdapter implements NewsSourcePort {

    private static final Logger log = LoggerFactory.getLogger(YouTubeApiAdapter.class);
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";
    private static final int MAX_RESULTS = 10;

    private final WebClient webClient;
    private final String apiKey;

    public YouTubeApiAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${pulsestack.youtube.api-key}") String apiKey
    ) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
    }

    @Override
    public List<NewsItem> fetchLatest(Channel channel) {
        log.info("Fetching from YouTube for channel: {}", channel.name());
        try {
            YouTubeResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", channel.name())
                            .queryParam("type", "video")
                            .queryParam("order", "date")
                            .queryParam("maxResults", MAX_RESULTS)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(YouTubeResponse.class)
                    .block();

            if (response == null || response.items() == null) {
                log.warn("Empty response from YouTube for channel: {}", channel.name());
                return List.of();
            }

            return response.items().stream()
                    .filter(item -> item.id() != null && item.id().videoId() != null)
                    .map(item -> mapToNewsItem(item, channel))
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch from YouTube for channel {}: {}", channel.name(), e.getMessage(), e);
            return List.of();
        }
    }

    private NewsItem mapToNewsItem(YouTubeItem item, Channel channel) {
        String videoId = item.id().videoId();
        String thumbnail = item.snippet().thumbnails() != null
                && item.snippet().thumbnails().medium() != null
                ? item.snippet().thumbnails().medium().url()
                : null;

        Instant publishedAt = parsePublishedAt(item.snippet().publishedAt());

        return new NewsItem(
                null,
                videoId,
                NewsSource.YOUTUBE,
                channel.id(),
                item.snippet().title(),
                "https://www.youtube.com/watch?v=" + videoId,
                thumbnail,
                item.snippet().channelTitle(),
                null,
                publishedAt,
                Instant.now()
        );
    }

    private Instant parsePublishedAt(String iso) {
        try {
            return Instant.parse(iso);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    @Override
    public NewsSource getSource() {
        return NewsSource.YOUTUBE;
    }
}
