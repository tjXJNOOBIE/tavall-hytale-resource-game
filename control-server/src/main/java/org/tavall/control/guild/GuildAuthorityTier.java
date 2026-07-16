package org.tavall.control.guild;

public enum GuildAuthorityTier {
    OUTSIDER(0),
    CITIZEN(1),
    TRUSTED(2),
    OFFICER(3),
    COUNCIL(4),
    RULER(5);

    private final int power;

    GuildAuthorityTier(int power) {
        this.power = power;
    }

    public boolean atLeast(GuildAuthorityTier requiredTier) {
        return power >= requiredTier.power;
    }
}
