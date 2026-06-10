package dev.pulsestack.ingestion.api;

import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.domain.model.NewsSource;
import dev.pulsestack.ingestion.api.dto.ExternalIngestRequest;
import dev.pulsestack.ingestion.domain.port.DuplicateChecker;
import dev.pulsestack.ingestion.domain.port.NewsEventPublisher;
import dev.pulsestack.ingestion.infrastructure.persistence.ChannelEntity;
import dev.pulsestack.ingestion.infrastructure.persistence.ChannelRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Erlaubt externen Services (StockPredictionGame, eigene Skripte, etc.)
 * direkt Events in den PulseStack-Feed einzuspeisen.
 *
 * Authentifizierung: einfaches Admin-Secret im Header "X-Ingest-Secret".
 * Service-to-Service — kein JWT nötig.
 *
 * Alle eingehenden Events durchlaufen denselben Pipeline:
 *   Redis-Dedup → Kafka raw-news → Processing-Service → WebSocket
 */
@RestController
@RequestMapping("/api/v1/ingest")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "External Ingest", description = "Service-to-Service Ingest für externe Event-Quellen")
public class ExternalIngestController {

    private static final Logger log = LoggerFactory.getLogger(ExternalIngestController.class);

    private final ChannelRepository channelRepository;
    private final DuplicateChecker duplicateChecker;
    private final NewsEventPublisher eventPublisher;
    private final String ingestSecret;

    public ExternalIngestController(
            ChannelRepository channelRepository,
            DuplicateChecker duplicateChecker,
            NewsEventPublisher eventPublisher,
            @Value("${pulsestack.ingest.secret:change-me-in-production}") String ingestSecret
    ) {
        this.channelRepository = channelRepository;
        this.duplicateChecker  = duplicateChecker;
        this.eventPublisher    = eventPublisher;
        this.ingestSecret      = ingestSecret;
    }

    @PostMapping("/external")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Externes Event einspeisen",
            description = "Nimmt ein Event von einem externen Service entgegen und schleust es durch die PulseStack-Pipeline (Dedup → Kafka → WebSocket)."
    )
    public void ingestExternal(
            @RequestHeader("X-Ingest-Secret") String secret,
            @Valid @RequestBody ExternalIngestRequest request
    ) {
        // Service-to-Service Auth
        if (!ingestSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid ingest secret");
        }

        // Channel per Name nachschlagen
        ChannelEntity channel = channelRepository.findByName(request.channelName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unknown channel: '" + request.channelName() + "'. Must be one of the 25 configured channels."
                ));

        // Dedup-Check — gleiche externalId wird nicht doppelt gepublished
        if (duplicateChecker.isAlreadySeen(request.externalId())) {
            log.debug("Duplicate external event skipped: {}", request.externalId());
            return;
        }

        NewsItem item = new NewsItem(
                null,
                request.externalId(),
                NewsSource.STOCK_PREDICTOR,
                channel.getId(),
                request.title(),
                request.url(),
                request.thumbnailUrl(),
                request.author(),
                request.score(),
                Instant.now(),
                Instant.now()
        );

        eventPublisher.publish(item);
        log.info("External event published to channel '{}': {}", request.channelName(), request.title());
    }
}
