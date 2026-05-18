package org.tavall.control.citizen;

public record CitizenStatBlock(
        int strength,
        int endurance,
        int agility,
        int discipline,
        int intelligence,
        int moraleResilience,
        int workEfficiency,
        int combatPotential
) {
    public CitizenStatBlock {
        strength = clamp(strength);
        endurance = clamp(endurance);
        agility = clamp(agility);
        discipline = clamp(discipline);
        intelligence = clamp(intelligence);
        moraleResilience = clamp(moraleResilience);
        workEfficiency = clamp(workEfficiency);
        combatPotential = clamp(combatPotential);
    }

    public static CitizenStatBlock balancedAdult() {
        return new CitizenStatBlock(50, 50, 45, 45, 45, 50, 50, 35);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
