package dev.pulsestack.processing.api;

import dev.pulsestack.processing.infrastructure.persistence.NewsItemEntity;
import dev.pulsestack.processing.infrastructure.persistence.NewsItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/news")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "News", description = "News feed per Channel")
public class NewsItemController {

    private final NewsItemRepository repository;

    public NewsItemController(NewsItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/channel/{channelId}")
    @Operation(summary = "News für einen Channel", description = "Liefert alle gespeicherten Items eines Channels, neueste zuerst.")
    public List<NewsItemEntity> getByChannel(@PathVariable UUID channelId) {
        return repository.findByChannelIdOrderByFetchedAtDesc(channelId);
    }
}
