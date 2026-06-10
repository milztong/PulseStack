package dev.pulsestack.processing.api;

import dev.pulsestack.processing.infrastructure.persistence.NewsItemEntity;
import dev.pulsestack.processing.infrastructure.persistence.NewsItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cross-Platform Volltextsuche über alle gespeicherten News-Items.
 * Durchsucht Titel aller Quellen (Reddit, YouTube, GitHub, NewsAPI) gleichzeitig.
 */
@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Search", description = "Cross-platform full-text search across all news sources")
public class SearchController {

    private static final int MAX_RESULTS = 50;

    private final NewsItemRepository repository;

    public SearchController(NewsItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(
            summary = "Suche über alle News-Quellen",
            description = "Durchsucht Titel aller gespeicherten Items über Reddit, YouTube, GitHub und NewsAPI."
    )
    public List<NewsItemEntity> search(
            @Parameter(description = "Suchbegriff (min. 2 Zeichen)", required = true)
            @RequestParam String q,

            @Parameter(description = "Max. Anzahl Ergebnisse (1-50)")
            @RequestParam(defaultValue = "20") int limit
    ) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(limit, 1), MAX_RESULTS);
        return repository.searchByTitle(q.trim(), PageRequest.of(0, safeLimit));
    }
}
