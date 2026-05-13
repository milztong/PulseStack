package dev.pulsestack.processing.api;

import dev.pulsestack.processing.infrastructure.persistence.NewsItemEntity;
import dev.pulsestack.processing.infrastructure.persistence.NewsItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/news")
@CrossOrigin(origins = "http://localhost:5173")
public class NewsItemController {

    private final NewsItemRepository repository;

    public NewsItemController(NewsItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/channel/{channelId}")
    public List<NewsItemEntity> getByChannel(@PathVariable UUID channelId) {
        return repository.findByChannelIdOrderByFetchedAtDesc(channelId);
    }
}
