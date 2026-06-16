package dev.pulsestack.chat.api;

import dev.pulsestack.chat.api.dto.ChatMessageResponse;
import dev.pulsestack.chat.domain.ChatMessageRepository;
import dev.pulsestack.chat.presence.PresenceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatHistoryController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ChatMessageRepository repository;
    private final PresenceService presenceService;

    public ChatHistoryController(ChatMessageRepository repository, PresenceService presenceService) {
        this.repository      = repository;
        this.presenceService = presenceService;
    }

    @GetMapping("/channel/{channelId}")
    public List<ChatMessageResponse> getHistory(
            @PathVariable UUID channelId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return repository
                .findByChannelIdOrderBySentAtDesc(channelId, PageRequest.of(0, safeLimit))
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @GetMapping("/channel/{channelId}/presence")
    public Set<Object> getPresence(@PathVariable UUID channelId) {
        return presenceService.getOnlineUsers(channelId);
    }
}
