package org.tavall.api.minecraft.guild;

import org.tavall.dependency.composition.IDependencyBundle;

public interface IGuildDependencies extends IDependencyBundle {
    IGuildStateStore guildStateStore();

    IGuildPlayerDirectory guildPlayerDirectory();

    IGuildRankPermissionPolicy guildRankPermissionPolicy();

    IGuildRoleSlotPolicy guildRoleSlotPolicy();

    IGuildCreationBuilder guildCreationBuilder();

    IGuildMetaDataBuilder guildMetaDataBuilder();

    IGuildSettingsBuilder guildSettingsBuilder();

    IPlayerGuildDataBuilder playerGuildDataBuilder();

    IGuildBankDataBuilder guildBankDataBuilder();

    IGuildBankTransactionBuilder guildBankTransactionBuilder();

    IGuildChatMessageBuilder guildChatMessageBuilder();

    IGuildContributionEventBuilder guildContributionEventBuilder();

    IGuildCreationHandler guildCreationHandler();

    IGuildMembershipHandler guildMembershipHandler();

    IGuildRoleHandler guildRoleHandler();

    IGuildBankHandler guildBankHandler();

    IGuildChatHandler guildChatHandler();

    IGuildContributionHandler guildContributionHandler();

    IGuildLeaderboardHandler guildLeaderboardHandler();
}
