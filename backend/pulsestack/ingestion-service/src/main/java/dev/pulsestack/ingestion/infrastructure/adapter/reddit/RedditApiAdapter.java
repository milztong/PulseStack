package dev.pulsestack.ingestion.infrastructure.adapter.reddit;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.domain.exception.NewsIngestionException;
import dev.pulsestack.ingestion.domain.port.NewsSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class RedditApiAdapter implements NewsSourcePort {

    private static final Logger log = LoggerFactory.getLogger(RedditApiAdapter.class);
    private static final String REDDIT_BASE_URL = "https://oauth.reddit.com";
    private static final int MAX_POSTS = 10;

    private final WebClient webClient;
    private final RedditTokenProvider tokenProvider;

    public RedditApiAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${pulsestack.reddit.base-url:https://oauth.reddit.com}") String baseUrl,
            RedditTokenProvider tokenProvider
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "PulseStack/1.0")
                .build();
        this.tokenProvider = tokenProvider;
    }

    @Override
    public List<NewsItem> fetchLatest(Channel channel) {
        log.info("Fetching from Reddit for channel: {}", channel.name());

        try {
            String token = tokenProvider.getAccessToken();

            RedditResponse response = webClient.get()
                    .uri("/r/{subreddit}/hot?limit={limit}",
                            channel.name(), MAX_POSTS)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(RedditResponse.class)
                    .block();

            if (response == null || response.data() == null) {
                log.warn("Empty response from Reddit for channel: {}", channel.name());
                return List.of();
            }

            return response.data().children().stream()
                    .map(child -> mapToNewsItem(child.data(), channel))
                    .toList();

        } catch (Exception e) {
            // Nie den ganzen Job crashen lassen – leere Liste + Log
            log.error("Failed to fetch from Reddit for channel {}: {}",
                    channel.name(), e.getMessage(), e);
            return List.of();
        }
    }

    private NewsItem mapToNewsItem(RedditPost post, Channel channel) {
        return NewsItem.of(
                post.id(),
                NewsSource.REDDIT,
                channel.id(),
                post.title(),
                "https://reddit.com" + post.permalink(),
                post.author(),
                post.score(),
                Instant.ofEpochSecond(post.createdUtc().longValue())
        );
    }

    @Override
    public NewsSource getSource() {
        return NewsSource.REDDIT;
    }
}
