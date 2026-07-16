package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.composition.IDependencyBundleAccess;

import java.util.Objects;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildCreationHandler.class)
public final class GuildCreationHandler implements IGuildCreationHandler, IDependencyInjectableConcrete, IDependencyBundleAccess<IGuildDependencies> {
    public IGuildDependencies dependencies() {
        return getDependencies();
    }

    private IGuildStateStore guildStateStore() {
        return dependencies().guildStateStore();
    }

    @Override
    public GuildMetaData createGuild(UUID creatorPlayerId, String name, String tag, String motto) {
        Objects.requireNonNull(creatorPlayerId, "creatorPlayerId");

        if (guildStateStore().findByMember(creatorPlayerId).isPresent()) {
            throw new IllegalStateException("Creator is already a member of a guild.");
        }

        GuildMetaData guild = dependencies().guildCreationBuilder()
                .creator(creatorPlayerId)
                .guildName(name)
                .tag(tag)
                .motto(motto)
                .build();
        return guildStateStore().save(guild);
    }
}

