package dev.pulsestack.processing.infrastructure.websocket;

import dev.pulsestack.domain.model.NewsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcastet neue News-Items per WebSocket STOMP an alle verbundenen Clients.
 * Topic: /topic/channel/{channelId}
 */
@Component
public class NewsWebSocketBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(NewsWebSocketBroadcaster.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NewsWebSocketBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(NewsItem newsItem) {
        String destination = "/topic/channel/" + newsItem.channelId();
        messagingTemplate.convertAndSend(destination, newsItem);
        log.debug("Broadcasted NewsItem to {}: externalId={}",
                destination, newsItem.externalId());
    }
}
