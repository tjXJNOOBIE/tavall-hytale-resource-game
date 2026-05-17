package com.tavall.resourcegame.middleware.authority;

public enum ControlAuthorityLevel {
    C1_LOCAL_EXECUTOR(1, false),
    C2_REGIONAL_OPERATOR(2, false),
    C3_REGIONAL_SUPERVISOR(3, false),
    C4_CLUSTER_GOVERNOR(4, false),
    C5_GLOBAL_AUTHORITY(5, true),
    C6_INTELLIGENCE_AUTHORITY(6, false);

    private final int level;
    private final boolean ownerAuthority;

    ControlAuthorityLevel(int level, boolean ownerAuthority) {
        this.level = level;
        this.ownerAuthority = ownerAuthority;
    }

    public boolean isAtLeast(ControlAuthorityLevel requiredLevel) {
        return level >= requiredLevel.level;
    }

    public boolean canSatisfyHumanOperationalLevel(ControlAuthorityLevel requiredLevel) {
        if (this == C6_INTELLIGENCE_AUTHORITY && requiredLevel != C6_INTELLIGENCE_AUTHORITY) {
            return false;
        }
        return isAtLeast(requiredLevel);
    }

    public int level() {
        return level;
    }

    public boolean ownerAuthority() {
        return ownerAuthority;
    }
}
