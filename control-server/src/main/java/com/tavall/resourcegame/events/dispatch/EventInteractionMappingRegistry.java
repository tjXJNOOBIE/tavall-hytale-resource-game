package com.tavall.resourcegame.events.dispatch;

import com.tavall.resourcegame.events.core.GameEventType;

import java.util.Locale;
import java.util.Map;

public final class EventInteractionMappingRegistry {
    private final Map<String, GameEventType> backendEventsByActionId;

    public EventInteractionMappingRegistry() {
        this(defaultMappings());
    }

    public EventInteractionMappingRegistry(Map<String, GameEventType> backendEventsByActionId) {
        this.backendEventsByActionId = backendEventsByActionId == null ? defaultMappings() : Map.copyOf(backendEventsByActionId);
    }

    public GameEventType eventTypeFor(String actionId) {
        return backendEventsByActionId.getOrDefault(normalize(actionId), GameEventType.CASTLE_INTERACTION_OPENED);
    }

    private String normalize(String actionId) {
        return actionId.toLowerCase(Locale.ROOT).trim();
    }

    private static Map<String, GameEventType> defaultMappings() {
        return Map.ofEntries(
                Map.entry("castle.select", GameEventType.CASTLE_SELECTED),
                Map.entry("castle.open", GameEventType.CASTLE_INTERACTION_OPENED),
                Map.entry("castle.interior.enter", GameEventType.CASTLE_INTERIOR_ENTER_REQUESTED),
                Map.entry("citizen.create", GameEventType.CITIZEN_CREATED),
                Map.entry("citizen.promote", GameEventType.CITIZEN_PROMOTED_TO_TROOP),
                Map.entry("resource.add", GameEventType.RESOURCE_ADDED),
                Map.entry("companion.training.start", GameEventType.COMPANION_TRAINING_STARTED),
                Map.entry("clock.tick", GameEventType.KINGDOM_CLOCK_TICK)
        );
    }
}
