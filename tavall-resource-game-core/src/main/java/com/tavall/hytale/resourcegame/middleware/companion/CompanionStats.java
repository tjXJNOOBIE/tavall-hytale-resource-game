package com.tavall.hytale.resourcegame.middleware.companion;

public record CompanionStats(
        double hp,
        double earthAttack,
        double earthDefense,
        double earthCrit,
        double arcaneAttack,
        double arcaneDefense,
        double arcaneCrit,
        double morale
) {
}
