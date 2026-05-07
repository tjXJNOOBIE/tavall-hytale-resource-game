package com.tavall.hytale.resourcegame.middleware.economy;

public record GuildTaxCollectionResult(
        int playerAmount,
        int guildTaxAmount,
        GuildTreasury updatedTreasury,
        GuildTaxTransaction transaction
) {
}
