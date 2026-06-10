package dev.pulsestack.ingestion.infrastructure.kafka;

import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.ingestion.domain.port.NewsEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisht NewsItems als JSON-Events in den Kafka-Topic "raw-news".
 *
 * Fehlerbehandlung: bei Send-Fehler wird geloggt, kein Crash.
 * Der Ingestion-Job laeuft weiter – verlorene Items werden beim naechsten Poll nachgeholt.
 */
@Component
public class KafkaNewsEventPublisher implements NewsEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaNewsEventPublisher.class);
    static final String TOPIC_RAW_NEWS = "raw-news";

    private final KafkaTemplate<String, NewsItem> kafkaTemplate;

    public KafkaNewsEventPublisher(KafkaTemplate<String, NewsItem> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(NewsItem newsItem) {
        // Key = channelId: alle Items eines Channels gehen in dieselbe Partition
        String partitionKey = newsItem.channelId().toString();

        kafkaTemplate.send(TOPIC_RAW_NEWS, partitionKey, newsItem)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish NewsItem externalId={} source={}: {}",
                                newsItem.externalId(), newsItem.source(), ex.getMessage(), ex);
                    } else {
                        log.debug("Published NewsItem externalId={} to partition {}",
                                newsItem.externalId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
