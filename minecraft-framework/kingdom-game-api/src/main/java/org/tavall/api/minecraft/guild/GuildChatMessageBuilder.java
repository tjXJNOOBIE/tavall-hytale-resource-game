package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildChatMessageBuilder.class)
public final class GuildChatMessageBuilder implements IGuildChatMessageBuilder, IDependencyInjectableConcrete {
    private final UUID messageId;
    private final GuildId guildId;
    private final UUID senderUniversalPlayerId;
    private final String channel;
    private final GuildRank minimumVisibleRank;
    private final String content;
    private final Instant createdAt;
    private final Map<String, String> metadata;

    public GuildChatMessageBuilder() {
        this(UUID.randomUUID(), null, null, "guild", GuildRank.TIER_I, "", Instant.now(), Map.of());
    }

    private GuildChatMessageBuilder(
            UUID messageId,
            GuildId guildId,
            UUID senderUniversalPlayerId,
            String channel,
            GuildRank minimumVisibleRank,
            String content,
            Instant createdAt,
            Map<String, String> metadata
    ) {
        this.messageId = messageId;
        this.guildId = guildId;
        this.senderUniversalPlayerId = senderUniversalPlayerId;
        this.channel = channel;
        this.minimumVisibleRank = minimumVisibleRank == null ? GuildRank.TIER_I : minimumVisibleRank;
        this.content = content;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildChatMessageBuilder messageId(UUID messageId) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder guildId(GuildId guildId) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder senderUniversalPlayerId(UUID senderUniversalPlayerId) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder channel(String channel) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder minimumVisibleRank(GuildRank minimumVisibleRank) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder content(String content) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder createdAt(Instant createdAt) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder metadata(Map<String, String> metadata) {
        return new GuildChatMessageBuilder(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }

    @Override
    public IGuildChatMessageBuilder copyOf(GuildChatMessage guildChatMessage) {
        Objects.requireNonNull(guildChatMessage, "guildChatMessage");
        return new GuildChatMessageBuilder(
                guildChatMessage.messageId(),
                guildChatMessage.guildId(),
                guildChatMessage.senderUniversalPlayerId(),
                guildChatMessage.channel(),
                guildChatMessage.minimumVisibleRank(),
                guildChatMessage.content(),
                guildChatMessage.createdAt(),
                guildChatMessage.metadata()
        );
    }

    @Override
    public GuildChatMessage build() {
        return new GuildChatMessage(messageId, guildId, senderUniversalPlayerId, channel, minimumVisibleRank, content, createdAt, metadata);
    }
}
