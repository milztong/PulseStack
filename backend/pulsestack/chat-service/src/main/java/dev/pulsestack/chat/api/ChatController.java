package dev.pulsestack.chat.api;

import dev.pulsestack.chat.api.dto.ChatMessageResponse;
import dev.pulsestack.chat.api.dto.SendMessageRequest;
import dev.pulsestack.chat.api.dto.TypingEvent;
import dev.pulsestack.chat.domain.ChatMessageEntity;
import dev.pulsestack.chat.domain.ChatMessageRepository;
import dev.pulsestack.chat.presence.PresenceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatMessageRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    public ChatController(
            ChatMessageRepository repository,
            SimpMessagingTemplate messagingTemplate,
            PresenceService presenceService
    ) {
        this.repository        = repository;
        this.messagingTemplate = messagingTemplate;
        this.presenceService   = presenceService;
    }

    @MessageMapping("chat.send")
    public void sendMessage(@Valid @Payload SendMessageRequest request, Principal principal) {
        String username = resolveName(principal);
        log.debug("Chat message from {} in channel {}", username, request.channelId());

        ChatMessageEntity saved = repository.save(
                new ChatMessageEntity(request.channelId(), username, request.content())
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.channelId(),
                ChatMessageResponse.from(saved)
        );
    }

    @MessageMapping("chat.typing")
    public void typing(@Payload TypingEvent event, Principal principal) {
        String username = resolveName(principal);
        // Kein Log-Spam bei typing events
        messagingTemplate.convertAndSend(
                "/topic/typing/" + event.channelId(),
                username
        );
    }

    @MessageMapping("presence.heartbeat")
    public void heartbeat(@Payload String channelIdStr, Principal principal) {
        try {
            UUID channelId = UUID.fromString(channelIdStr);
            String username = resolveName(principal);
            presenceService.heartbeat(channelId, username);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid channelId in heartbeat: {}", channelIdStr);
        }
    }

    private String resolveName(Principal principal) {
        return principal != null ? principal.getName() : "anonymous";
    }
}
