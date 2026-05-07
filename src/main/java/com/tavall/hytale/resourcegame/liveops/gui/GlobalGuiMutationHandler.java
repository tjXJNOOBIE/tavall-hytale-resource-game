package com.tavall.hytale.resourcegame.liveops.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.events.core.AbstractGameEvent;
import com.tavall.hytale.resourcegame.events.core.BasicGameEvent;
import com.tavall.hytale.resourcegame.events.core.EventSource;
import com.tavall.hytale.resourcegame.events.core.GameEventType;
import com.tavall.hytale.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigValidationException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class GlobalGuiMutationHandler {
    private final GlobalGuiRepository repository;
    private final GlobalGuiRegistry registry;
    private final GlobalGuiChangePublisher publisher;
    private final ObjectMapper objectMapper;
    private final Optional<GameEventDispatchHandler> eventDispatchHandler;

    public GlobalGuiMutationHandler(
            GlobalGuiRepository repository,
            GlobalGuiRegistry registry,
            GlobalGuiChangePublisher publisher,
            ObjectMapper objectMapper,
            GameEventDispatchHandler eventDispatchHandler
    ) {
        this.repository = repository;
        this.registry = registry;
        this.publisher = publisher;
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.eventDispatchHandler = Optional.ofNullable(eventDispatchHandler);
    }

    public GlobalGuiDefinition applyChange(GlobalGuiChangeRequest request) {
        return applyChange(request, Instant.now());
    }

    public GlobalGuiDefinition applyChange(GlobalGuiChangeRequest request, Instant now) {
        validateLayout(request.layoutJson());
        GlobalGuiDefinition definition = repository.findByKey(request.guiKey())
                .map(existing -> existing.withVersionedLayout(request.title(), request.layoutJson(), request.enabled(), request.updatedBy(), now))
                .orElseGet(() -> new GlobalGuiDefinition(UUID.randomUUID(), request.guiKey(), request.title(), request.layoutJson(), 1L, request.enabled(), now, request.updatedBy()));
        repository.save(definition);
        registry.apply(definition);
        GlobalGuiChange change = new GlobalGuiChange(definition, now, request.metadata());
        publisher.publish(change);
        dispatchUpdateEvent(definition, request.metadata());
        return definition;
    }

    public List<GlobalGuiDefinition> reloadEnabled() {
        List<GlobalGuiDefinition> definitions = repository.findEnabled();
        registry.reload(definitions);
        return definitions;
    }

    private void validateLayout(String layoutJson) {
        try {
            objectMapper.readTree(layoutJson);
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Global GUI layoutJson must be valid JSON.", ex);
        }
    }

    private void dispatchUpdateEvent(GlobalGuiDefinition definition, Map<String, String> requestMetadata) {
        eventDispatchHandler.ifPresent(dispatcher -> {
            Map<String, String> attributes = new LinkedHashMap<>(requestMetadata);
            attributes.put("guiKey", definition.guiKey());
            attributes.put("version", Long.toString(definition.version()));
            attributes.put("enabled", Boolean.toString(definition.enabled()));
            dispatcher.dispatch(new BasicGameEvent(GameEventType.GLOBAL_GUI_UPDATED, actorId(definition.updatedBy()), EventSource.ADMIN_GUI, attributes));
        });
    }

    private UUID actorId(String updatedBy) {
        if (updatedBy == null || updatedBy.isBlank()) {
            return AbstractGameEvent.SYSTEM_ACTOR_ID;
        }
        try {
            return UUID.fromString(updatedBy);
        } catch (IllegalArgumentException ignored) {
            return UUID.nameUUIDFromBytes(("global-gui:" + updatedBy).getBytes(StandardCharsets.UTF_8));
        }
    }
}
