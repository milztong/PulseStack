package dev.pulsestack.chat.api.dto;

import java.util.UUID;

/**
 * Wird vom Client gesendet wenn der User tippt.
 * Server broadcastet es an alle anderen Subscriber des Channels.
 */
public record TypingEvent(UUID channelId, String username) {}
