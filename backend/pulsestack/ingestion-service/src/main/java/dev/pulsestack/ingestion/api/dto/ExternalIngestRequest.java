package dev.pulsestack.ingestion.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExternalIngestRequest(
        @NotBlank String channelName,
        @NotBlank String externalId,
        @NotBlank @Size(max = 1000) String title,
        @NotBlank @Size(max = 2000) String url,
        String thumbnailUrl,
        String author,
        Integer score
) {}
