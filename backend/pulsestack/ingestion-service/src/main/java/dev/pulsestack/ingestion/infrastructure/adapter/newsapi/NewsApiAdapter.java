package dev.pulsestack.ingestion.infrastructure.adapter.newsapi;

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

/**
 * Fragt newsapi.org nach aktuellen Schlagzeilen zu einem Thema.
 * Nutzt /v2/everything mit sortBy=publishedAt für frische Inhalte.
 * Nur aktiv wenn pulsestack.newsapi.key gesetzt ist.
 */
@Component
@ConditionalOnProperty(name = "pulsestack.newsapi.key")
public class NewsApiAdapter implements NewsSourcePort {

    private static final Logger log = LoggerFactory.getLogger(NewsApiAdapter.class);
    private static final String BASE_URL = "https://newsapi.org/v2";
    private static final int PAGE_SIZE = 10;

    private final WebClient webClient;
    private final String apiKey;

    public NewsApiAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${pulsestack.newsapi.key}") String apiKey
    ) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
    }

    @Override
    public List<NewsItem> fetchLatest(Channel channel) {
        log.info("Fetching from NewsAPI for channel: {}", channel.name());
        try {
            NewsApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/everything")
                            .queryParam("q", channel.name())
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("pageSize", PAGE_SIZE)
                            .queryParam("language", "en")
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiResponse.class)
                    .block();

            if (response == null || response.articles() == null) {
                log.warn("Empty response from NewsAPI for channel: {}", channel.name());
                return List.of();
            }

            return response.articles().stream()
                    .filter(a -> a.url() != null && a.title() != null
                            && !a.title().equals("[Removed]"))
                    .map(a -> mapToNewsItem(a, channel))
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch from NewsAPI for channel {}: {}", channel.name(), e.getMessage(), e);
            return List.of();
        }
    }

    private NewsItem mapToNewsItem(NewsApiArticle article, Channel channel) {
        // externalId = URL-Hash, da NewsAPI keine eigene ID zurückgibt
        String externalId = "newsapi-" + Math.abs(article.url().hashCode());

        return new NewsItem(
                null,
                externalId,
                NewsSource.NEWSAPI,
                channel.id(),
                article.title(),
                article.url(),
                article.urlToImage(),
                resolveAuthor(article),
                null,
                parseInstant(article.publishedAt()),
                Instant.now()
        );
    }

    private String resolveAuthor(NewsApiArticle article) {
        if (article.author() != null && !article.author().isBlank()) return article.author();
        if (article.source() != null && article.source().name() != null) return article.source().name();
        return null;
    }

    private Instant parseInstant(String iso) {
        try { return Instant.parse(iso); } catch (Exception e) { return Instant.now(); }
    }

    @Override
    public NewsSource getSource() {
        return NewsSource.NEWSAPI;
    }
}
