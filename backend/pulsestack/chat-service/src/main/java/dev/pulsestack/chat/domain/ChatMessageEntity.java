package dev.pulsestack.chat.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt = Instant.now();

    protected ChatMessageEntity() {}

    public ChatMessageEntity(UUID channelId, String senderName, String content) {
        this.channelId  = channelId;
        this.senderName = senderName;
        this.content    = content;
    }

    public UUID getId()          { return id; }
    public UUID getChannelId()   { return channelId; }
    public String getSenderName(){ return senderName; }
    public String getContent()   { return content; }
    public Instant getSentAt()   { return sentAt; }
}
