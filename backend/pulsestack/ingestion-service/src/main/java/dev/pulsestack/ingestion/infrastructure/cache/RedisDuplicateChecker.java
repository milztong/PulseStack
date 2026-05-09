package dev.pulsestack.ingestion.infrastructure.cache;

import dev.pulsestack.ingestion.domain.port.DuplicateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Implementiert DuplicateChecker mit Redis SETNX.
 *
 * SETNX ist atomar: Set if Not eXists.
 * Gibt true zurueck wenn der Key NICHT gesetzt wurde (= schon vorhanden = Duplikat).
 * TTL: nach 24h werden Keys automatisch geloescht.
 */
@Component
public class RedisDuplicateChecker implements DuplicateChecker {

    private static final Logger log = LoggerFactory.getLogger(RedisDuplicateChecker.class);
    private static final String KEY_PREFIX = "pulsestack:seen:";

    private final StringRedisTemplate redisTemplate;
    private final Duration dedupTtl;

    public RedisDuplicateChecker(
            StringRedisTemplate redisTemplate,
            @Value("${pulsestack.ingestion.dedup-ttl-hours:24}") long dedupTtlHours
    ) {
        this.redisTemplate = redisTemplate;
        this.dedupTtl = Duration.ofHours(dedupTtlHours);
    }

    @Override
    public boolean isAlreadySeen(String externalId) {
        String key = KEY_PREFIX + externalId;

        // setIfAbsent = SETNX: gibt true wenn Key NEU gesetzt wurde (= noch nicht gesehen)
        Boolean wasNew = redisTemplate.opsForValue().setIfAbsent(key, "1", dedupTtl);

        boolean isDuplicate = !Boolean.TRUE.equals(wasNew);
        if (isDuplicate) {
            log.trace("Duplicate detected, skipping: {}", externalId);
        }
        return isDuplicate;
    }
}
