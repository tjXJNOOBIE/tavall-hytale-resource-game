package com.tavall.hytale.resourcegame.events.dispatch;

import com.tavall.hytale.resourcegame.events.core.BasicGameEvent;
import com.tavall.hytale.resourcegame.events.core.EventSource;
import com.tavall.hytale.resourcegame.events.core.GameEvent;
import com.tavall.hytale.resourcegame.events.core.GameEventResult;
import com.tavall.hytale.resourcegame.events.core.GameEventType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class InteractionBridgeHandler {
    private final GameEventDispatchHandler dispatchHandler;
    private final Map<String, GameEventType> backendEventsByActionId;

    public InteractionBridgeHandler(GameEventDispatchHandler dispatchHandler) {
        this(dispatchHandler, defaultMappings());
    }

    public InteractionBridgeHandler(GameEventDispatchHandler dispatchHandler, Map<String, GameEventType> backendEventsByActionId) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.backendEventsByActionId = backendEventsByActionId == null ? defaultMappings() : Map.copyOf(backendEventsByActionId);
    }

    public GameEvent translate(FrontendInteractionRequest request) {
        GameEventType eventType = backendEventsByActionId.getOrDefault(normalize(request.actionId()), GameEventType.CASTLE_INTERACTION_OPENED);
        Map<String, String> attributes = new LinkedHashMap<>(request.payload());
        attributes.put("interactionId", request.interactionId().toString());
        attributes.put("actionId", request.actionId());
        attributes.put("targetId", request.targetId());
        attributes.put("occurredAt", request.occurredAt().toString());
        return new BasicGameEvent(eventType, request.actorId(), EventSource.FRONTEND_INTERACTION, attributes);
    }

    public GameEventResult submitInteraction(FrontendInteractionRequest request) {
        return dispatchHandler.dispatch(translate(request));
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
