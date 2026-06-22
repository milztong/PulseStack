package dev.pulsestack.chat.presence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock HashOperations<String, Object, Object> hashOps;

    private PresenceService presenceService;
    private final UUID channelId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(redis.opsForHash()).thenReturn(hashOps);
        presenceService = new PresenceService(redis, 60L);
    }

    @Test
    void heartbeat_storesUserInRedisHash() {
        presenceService.heartbeat(channelId, "alice");

        verify(hashOps).put(eq("presence:channel:" + channelId), eq("alice"), any(String.class));
    }

    @Test
    void heartbeat_setsExpiry() {
        presenceService.heartbeat(channelId, "alice");

        verify(redis).expire(eq("presence:channel:" + channelId), eq(Duration.ofSeconds(60)));
    }

    @Test
    void leave_removesUserFromHash() {
        presenceService.leave(channelId, "alice");

        verify(hashOps).delete("presence:channel:" + channelId, "alice");
    }

    @Test
    void getOnlineUsers_returnsKeySetFromHash() {
        when(hashOps.entries("presence:channel:" + channelId))
                .thenReturn(Map.of("alice", "123456", "bob", "789012"));

        var users = presenceService.getOnlineUsers(channelId);

        assertThat(users).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    void getOnlineUsers_emptyChannel_returnsEmptySet() {
        when(hashOps.entries("presence:channel:" + channelId)).thenReturn(Map.of());

        var users = presenceService.getOnlineUsers(channelId);

        assertThat(users).isEmpty();
    }
}
