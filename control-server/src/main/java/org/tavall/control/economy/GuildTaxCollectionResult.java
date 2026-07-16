package org.tavall.control.economy;

public record GuildTaxCollectionResult(
        int playerAmount,
        int guildTaxAmount,
        GuildTreasury updatedTreasury,
        GuildTaxTransaction transaction
) {
}
