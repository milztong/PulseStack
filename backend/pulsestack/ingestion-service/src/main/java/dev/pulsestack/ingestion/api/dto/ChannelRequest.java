package dev.pulsestack.ingestion.api.dto;

public record ChannelRequest(
        String name,
        String displayName,
        String description
) {}
