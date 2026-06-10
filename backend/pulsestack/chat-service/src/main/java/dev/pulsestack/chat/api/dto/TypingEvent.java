package dev.pulsestack.chat.api.dto;

import java.util.UUID;

public record TypingEvent(UUID channelId, String username) {}
