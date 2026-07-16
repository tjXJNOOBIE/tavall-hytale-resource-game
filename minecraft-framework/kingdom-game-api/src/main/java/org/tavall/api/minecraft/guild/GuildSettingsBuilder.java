package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.util.Map;

@DelegatesToInterface(getLinkedInterface = IGuildSettingsBuilder.class)
public final class GuildSettingsBuilder implements IGuildSettingsBuilder, IDependencyInjectableConcrete {
    private final boolean publicGuild;
    private final boolean inviteOnly;
    private final Map<String, String> metadata;

    public GuildSettingsBuilder() {
        this(true, false, Map.of());
    }

    private GuildSettingsBuilder(boolean publicGuild, boolean inviteOnly, Map<String, String> metadata) {
        this.publicGuild = publicGuild;
        this.inviteOnly = inviteOnly;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildSettingsBuilder publicGuild(boolean publicGuild) {
        return new GuildSettingsBuilder(publicGuild, inviteOnly, metadata);
    }

    @Override
    public IGuildSettingsBuilder inviteOnly(boolean inviteOnly) {
        return new GuildSettingsBuilder(publicGuild, inviteOnly, metadata);
    }

    @Override
    public IGuildSettingsBuilder metadata(Map<String, String> metadata) {
        return new GuildSettingsBuilder(publicGuild, inviteOnly, metadata);
    }

    @Override
    public IGuildSettingsBuilder copyOf(GuildSettings settings) {
        return new GuildSettingsBuilder(settings.publicGuild(), settings.inviteOnly(), settings.metadata());
    }

    @Override
    public GuildSettings build() {
        return new GuildSettings(publicGuild, inviteOnly, metadata);
    }
}
