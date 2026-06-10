package dev.pulsestack.ingestion.infrastructure.adapter.newsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsApiResponse(
        String status,
        int totalResults,
        List<NewsApiArticle> articles
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record NewsApiArticle(
        NewsApiSource source,
        String author,
        String title,
        String url,
        String urlToImage,
        String publishedAt
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record NewsApiSource(String id, String name) {}
