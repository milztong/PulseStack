package dev.pulsestack.domain.model;

import java.util.UUID;

public record Channel(
        UUID id,
        String name,
        String displayName,
        String description
) {}
