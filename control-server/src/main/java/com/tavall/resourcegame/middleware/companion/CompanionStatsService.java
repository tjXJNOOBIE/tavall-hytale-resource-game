package com.tavall.resourcegame.middleware.companion;

public final class CompanionStatsService {
    public CompanionStats calculateCompanionStats(CompanionData companionData) {
        CompanionBaseAttributes base = companionData.baseAttributes();
        double moraleModifier = calculateMoraleModifier(companionData);
        double typeEarth = switch (companionData.type()) {
            case BRUTE -> 1.25;
            case BRAWLER -> 1.15;
            case HEALER -> 0.75;
            case ARCANE -> 0.8;
        };
        double typeArcane = switch (companionData.type()) {
            case ARCANE -> 1.3;
            case HEALER -> 1.15;
            case BRAWLER -> 0.75;
            case BRUTE -> 0.65;
        };
        double levelScaling = companionData.level() * 2.0d;
        double hp = 80.0d + levelScaling + base.strength() * 8.0d;
        double earthAttack = base.strength() * typeEarth + companionData.level();
        double earthDefense = (base.strength() * 0.8d + companionData.level()) * moraleModifier;
        double earthCrit = Math.min(0.75d, (base.agility() * 0.01d) + typeCritModifier(companionData.type()));
        double arcaneAttack = base.intel() * typeArcane + companionData.level();
        double arcaneDefense = (base.intel() + companionData.level() * 0.75d) * moraleModifier;
        double arcaneCrit = Math.min(0.75d, (base.agility() * 0.008d) + typeArcaneCritModifier(companionData.type()));
        double morale = 50.0d * moraleModifier + companionData.level() * 0.25d;
        return new CompanionStats(hp, earthAttack, earthDefense, earthCrit, arcaneAttack, arcaneDefense, arcaneCrit, morale);
    }

    public double calculateMoraleModifier(CompanionData companionData) {
        return switch (companionData.moraleState()) {
            case HIGH -> 1.15d;
            case MEDIUM -> 1.0d;
            case LOW -> 0.85d;
            case POOR -> 0.6d;
        };
    }

    private double typeCritModifier(CompanionType type) {
        return switch (type) {
            case BRAWLER -> 0.12d;
            case BRUTE -> 0.04d;
            case HEALER, ARCANE -> 0.03d;
        };
    }

    private double typeArcaneCritModifier(CompanionType type) {
        return switch (type) {
            case ARCANE -> 0.11d;
            case HEALER -> 0.08d;
            case BRAWLER, BRUTE -> 0.02d;
        };
    }
}
