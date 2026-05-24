package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.Map;
import java.util.UUID;

public interface IGuildCreationBuilder extends IDependencyInjectableInterface {
    IGuildCreationBuilder creator(UUID creatorPlayerId);

    IGuildCreationBuilder guildName(String guildName);

    IGuildCreationBuilder tag(String tag);

    IGuildCreationBuilder motto(String motto);

    IGuildCreationBuilder publicGuild(boolean publicGuild);

    IGuildCreationBuilder inviteOnly(boolean inviteOnly);

    IGuildCreationBuilder startingCoins(long startingCoins);

    IGuildCreationBuilder metadata(Map<String, String> metadata);

    IGuildCreationBuilder copyOf(GuildMetaData guildMetaData);

    GuildMetaData build();
}
