package dev.pulsestack.processing.infrastructure.kafka;

import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.processing.application.service.NewsProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Konsumiert raw-news Events aus Kafka.
 *
 * SRP: Nur Empfang und Weiterleitung – keine Business-Logik hier.
 * Die eigentliche Verarbeitung delegiert er an NewsProcessingService.
 */
@Component
public class NewsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NewsEventConsumer.class);

    private final NewsProcessingService processingService;

    public NewsEventConsumer(NewsProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "raw-news",
            groupId = "processing-service-group"
    )
    public void consume(NewsItem newsItem) {
        log.debug("Received NewsItem from Kafka: externalId={} source={} channel={}",
                newsItem.externalId(), newsItem.source(), newsItem.channelId());
        processingService.process(newsItem);
    }
}
