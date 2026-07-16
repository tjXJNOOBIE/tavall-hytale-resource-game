package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface IGuildChatMessageBuilder extends IDependencyInjectableInterface {
    IGuildChatMessageBuilder messageId(UUID messageId);

    IGuildChatMessageBuilder guildId(GuildId guildId);

    IGuildChatMessageBuilder senderUniversalPlayerId(UUID senderUniversalPlayerId);

    IGuildChatMessageBuilder channel(String channel);

    IGuildChatMessageBuilder minimumVisibleRank(GuildRank minimumVisibleRank);

    IGuildChatMessageBuilder content(String content);

    IGuildChatMessageBuilder createdAt(Instant createdAt);

    IGuildChatMessageBuilder metadata(Map<String, String> metadata);

    IGuildChatMessageBuilder copyOf(GuildChatMessage guildChatMessage);

    GuildChatMessage build();
}
