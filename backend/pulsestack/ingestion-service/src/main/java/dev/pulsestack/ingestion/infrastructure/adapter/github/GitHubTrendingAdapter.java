package dev.pulsestack.ingestion.infrastructure.adapter.github;

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
@ConditionalOnProperty(name = "pulsestack.github.token")
public class GitHubTrendingAdapter implements NewsSourcePort {

    private static final Logger log = LoggerFactory.getLogger(GitHubTrendingAdapter.class);
    private static final String BASE_URL = "https://api.github.com";
    private static final int MAX_RESULTS = 10;

    private final WebClient webClient;

    public GitHubTrendingAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${pulsestack.github.token}") String token
    ) {
        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    @Override
    public List<NewsItem> fetchLatest(Channel channel) {
        log.info("Fetching from GitHub for channel: {}", channel.name());
        try {
            GitHubSearchResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", channel.name())
                            .queryParam("sort", "stars")
                            .queryParam("order", "desc")
                            .queryParam("per_page", MAX_RESULTS)
                            .build())
                    .retrieve()
                    .bodyToMono(GitHubSearchResponse.class)
                    .block();

            if (response == null || response.items() == null) {
                log.warn("Empty response from GitHub for channel: {}", channel.name());
                return List.of();
            }

            return response.items().stream()
                    .map(repo -> mapToNewsItem(repo, channel))
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch from GitHub for channel {}: {}", channel.name(), e.getMessage(), e);
            return List.of();
        }
    }

    private NewsItem mapToNewsItem(GitHubRepo repo, Channel channel) {
        String title = repo.fullName() + (repo.description() != null
                ? " — " + truncate(repo.description(), 150)
                : "");
        String author = repo.owner() != null ? repo.owner().login() : null;

        Instant publishedAt = parseInstant(repo.createdAt());

        return new NewsItem(
                null,
                String.valueOf(repo.id()),
                NewsSource.GITHUB,
                channel.id(),
                title,
                repo.htmlUrl(),
                null,
                author,
                repo.stargazersCount(),
                publishedAt,
                Instant.now()
        );
    }

    private Instant parseInstant(String iso) {
        try { return Instant.parse(iso); } catch (Exception e) { return Instant.now(); }
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    @Override
    public NewsSource getSource() {
        return NewsSource.GITHUB;
    }
}
