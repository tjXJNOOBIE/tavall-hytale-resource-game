package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.annotations.Inject;

@DelegatesToInterface(getLinkedInterface = IGuildDependencies.class)
public final class GuildDependencies implements IGuildDependencies, IDependencyInjectableConcrete {
    @Inject
    private IGuildStateStore guildStateStore;

    @Inject
    private IGuildPlayerDirectory guildPlayerDirectory;

    @Inject
    private IGuildRankPermissionPolicy guildRankPermissionPolicy;

    @Inject
    private IGuildRoleSlotPolicy guildRoleSlotPolicy;

    @Inject
    private IGuildCreationBuilder guildCreationBuilder;

    @Inject
    private IGuildMetaDataBuilder guildMetaDataBuilder;

    @Inject
    private IGuildSettingsBuilder guildSettingsBuilder;

    @Inject
    private IPlayerGuildDataBuilder playerGuildDataBuilder;

    @Inject
    private IGuildBankDataBuilder guildBankDataBuilder;

    @Inject
    private IGuildBankTransactionBuilder guildBankTransactionBuilder;

    @Inject
    private IGuildChatMessageBuilder guildChatMessageBuilder;

    @Inject
    private IGuildContributionEventBuilder guildContributionEventBuilder;

    @Inject
    private IGuildCreationHandler guildCreationHandler;

    @Inject
    private IGuildMembershipHandler guildMembershipHandler;

    @Inject
    private IGuildRoleHandler guildRoleHandler;

    @Inject
    private IGuildBankHandler guildBankHandler;

    @Inject
    private IGuildChatHandler guildChatHandler;

    @Inject
    private IGuildContributionHandler guildContributionHandler;

    @Inject
    private IGuildLeaderboardHandler guildLeaderboardHandler;

    @Override
    public IGuildStateStore guildStateStore() {
        return guildStateStore;
    }

    @Override
    public IGuildPlayerDirectory guildPlayerDirectory() {
        return guildPlayerDirectory;
    }

    @Override
    public IGuildRankPermissionPolicy guildRankPermissionPolicy() {
        return guildRankPermissionPolicy;
    }

    @Override
    public IGuildRoleSlotPolicy guildRoleSlotPolicy() {
        return guildRoleSlotPolicy;
    }

    @Override
    public IGuildCreationBuilder guildCreationBuilder() {
        return guildCreationBuilder;
    }

    @Override
    public IGuildMetaDataBuilder guildMetaDataBuilder() {
        return guildMetaDataBuilder;
    }

    @Override
    public IGuildSettingsBuilder guildSettingsBuilder() {
        return guildSettingsBuilder;
    }

    @Override
    public IPlayerGuildDataBuilder playerGuildDataBuilder() {
        return playerGuildDataBuilder;
    }

    @Override
    public IGuildBankDataBuilder guildBankDataBuilder() {
        return guildBankDataBuilder;
    }

    @Override
    public IGuildBankTransactionBuilder guildBankTransactionBuilder() {
        return guildBankTransactionBuilder;
    }

    @Override
    public IGuildChatMessageBuilder guildChatMessageBuilder() {
        return guildChatMessageBuilder;
    }

    @Override
    public IGuildContributionEventBuilder guildContributionEventBuilder() {
        return guildContributionEventBuilder;
    }

    @Override
    public IGuildCreationHandler guildCreationHandler() {
        return guildCreationHandler;
    }

    @Override
    public IGuildMembershipHandler guildMembershipHandler() {
        return guildMembershipHandler;
    }

    @Override
    public IGuildRoleHandler guildRoleHandler() {
        return guildRoleHandler;
    }

    @Override
    public IGuildBankHandler guildBankHandler() {
        return guildBankHandler;
    }

    @Override
    public IGuildChatHandler guildChatHandler() {
        return guildChatHandler;
    }

    @Override
    public IGuildContributionHandler guildContributionHandler() {
        return guildContributionHandler;
    }

    @Override
    public IGuildLeaderboardHandler guildLeaderboardHandler() {
        return guildLeaderboardHandler;
    }
}

