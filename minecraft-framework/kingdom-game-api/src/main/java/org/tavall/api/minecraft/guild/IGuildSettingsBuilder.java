package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.Map;

public interface IGuildSettingsBuilder extends IDependencyInjectableInterface {
    IGuildSettingsBuilder publicGuild(boolean publicGuild);

    IGuildSettingsBuilder inviteOnly(boolean inviteOnly);

    IGuildSettingsBuilder metadata(Map<String, String> metadata);

    IGuildSettingsBuilder copyOf(GuildSettings settings);

    GuildSettings build();
}
