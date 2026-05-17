package com.tavall.resourcegame.middleware.economy;

public record GuildTaxCollectionResult(
        int playerAmount,
        int guildTaxAmount,
        GuildTreasury updatedTreasury,
        GuildTaxTransaction transaction
) {
}
