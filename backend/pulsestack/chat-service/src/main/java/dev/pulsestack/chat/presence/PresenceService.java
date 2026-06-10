package dev.pulsestack.chat.presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);
    private static final String KEY_PREFIX = "presence:channel:";

    private final StringRedisTemplate redis;
    private final Duration expiry;

    public PresenceService(
            StringRedisTemplate redis,
            @Value("${pulsestack.presence.expiry-seconds:60}") long expirySecs
    ) {
        this.redis  = redis;
        this.expiry = Duration.ofSeconds(expirySecs);
    }

    public void heartbeat(UUID channelId, String username) {
        String key = KEY_PREFIX + channelId;
        redis.opsForHash().put(key, username, String.valueOf(System.currentTimeMillis()));
        redis.expire(key, expiry);
        log.debug("Presence heartbeat: channel={} user={}", channelId, username);
    }

    public void leave(UUID channelId, String username) {
        redis.opsForHash().delete(KEY_PREFIX + channelId, username);
        log.debug("Presence leave: channel={} user={}", channelId, username);
    }

    public Set<Object> getOnlineUsers(UUID channelId) {
        Map<Object, Object> entries = redis.opsForHash().entries(KEY_PREFIX + channelId);
        return entries.keySet();
    }
}
