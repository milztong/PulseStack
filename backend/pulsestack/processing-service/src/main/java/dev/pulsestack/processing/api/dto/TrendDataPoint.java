package dev.pulsestack.processing.api.dto;

public record TrendDataPoint(
        String channelId,
        String channelName,
        long reddit,
        long youtube,
        long github,
        long newsapi
) {}
