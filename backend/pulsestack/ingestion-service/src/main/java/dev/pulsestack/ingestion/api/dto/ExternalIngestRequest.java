package dev.pulsestack.ingestion.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload für externe Services (z.B. StockPredictionGame) die Events
 * direkt in den PulseStack-Feed einspeisen wollen.
 *
 * channelName muss einem der 25 bekannten Channel-Namen entsprechen (z.B. "finance").
 * externalId muss pro source eindeutig sein — wird für Redis-Dedup verwendet.
 */
public record ExternalIngestRequest(
        @NotBlank String channelName,
        @NotBlank String externalId,
        @NotBlank @Size(max = 1000) String title,
        @NotBlank @Size(max = 2000) String url,
        String thumbnailUrl,
        String author,
        Integer score
) {}
