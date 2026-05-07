package com.tavall.hytale.resourcegame.liveops.config;

public enum GameSystemToggle {
    CASTLE_PLACEMENT("castle.placement.enabled"),
    CASTLE_RELOCATION("castle.relocation.enabled"),
    INTERIOR_ENTRY("castle.interior.enabled"),
    CITIZEN_AGING("citizens.aging.enabled"),
    CITIZEN_JOBS("citizens.jobs.enabled"),
    TROOP_PROMOTION("citizens.promotion.enabled"),
    COMPANION_TRAINING("companions.training.enabled"),
    WISDOM_WELL("companions.wisdomWell.enabled"),
    RESOURCE_GATHERING("resources.gathering.enabled"),
    KINGDOM_CLOCK("events.kingdomClock.enabled"),
    REMOTE_TESTING("remote.testing.enabled"),
    DEBUG_COMMANDS("debug.commands.enabled");

    private final String configKey;

    GameSystemToggle(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }
}
