package org.tavall.control.castle;

public record DefensiveStats(
        int armor,
        int shield,
        int siegeResistance
) {
    public DefensiveStats {
        armor = Math.max(0, armor);
        shield = Math.max(0, shield);
        siegeResistance = Math.max(0, siegeResistance);
    }
}
