package com.tavall.hytale.resourcegame.liveops.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.events.core.AbstractGameEvent;
import com.tavall.hytale.resourcegame.events.core.BasicGameEvent;
import com.tavall.hytale.resourcegame.events.core.EventSource;
import com.tavall.hytale.resourcegame.events.core.GameEventType;
import com.tavall.hytale.resourcegame.events.dispatch.GameEventDispatchHandler;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class LiveConfigMutationHandler {
    private final LiveConfigRepository repository;
    private final LiveConfigRegistry registry;
    private final LiveConfigChangePublisher publisher;
    private final ObjectMapper objectMapper;
    private final Optional<GameEventDispatchHandler> eventDispatchHandler;

    public LiveConfigMutationHandler(
            LiveConfigRepository repository,
            LiveConfigRegistry registry,
            LiveConfigChangePublisher publisher,
            ObjectMapper objectMapper,
            GameEventDispatchHandler eventDispatchHandler
    ) {
        this.repository = repository;
        this.registry = registry;
        this.publisher = publisher;
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.eventDispatchHandler = Optional.ofNullable(eventDispatchHandler);
    }

    public LiveConfigEntry applyChange(LiveConfigChangeRequest request) {
        return applyChange(request, Instant.now());
    }

    public LiveConfigEntry applyChange(LiveConfigChangeRequest request, Instant now) {
        validateJson(request.valueJson());
        LiveConfigEntry entry = repository.findByKey(request.key(), request.environment())
                .map(existing -> existing.withVersionedChange(request.type(), request.valueJson(), request.enabled(), request.updatedBy(), now, request.description(), request.rolloutStrategy()))
                .orElseGet(() -> new LiveConfigEntry(
                        UUID.randomUUID(),
                        request.key(),
                        request.type(),
                        request.valueJson(),
                        request.enabled(),
                        request.environment(),
                        1L,
                        request.updatedBy(),
                        now,
                        request.description(),
                        request.rolloutStrategy()
                ));
        repository.save(entry);
        registry.apply(entry);
        LiveConfigChange change = new LiveConfigChange(entry, "upsert", now, request.metadata());
        publisher.publish(change);
        dispatchChangeEvent(entry, request.metadata());
        return entry;
    }

    public List<LiveConfigEntry> reloadEnvironment(String environment) {
        List<LiveConfigEntry> entries = repository.findByEnvironment(environment);
        registry.reload(entries);
        eventDispatchHandler.ifPresent(dispatcher -> dispatcher.dispatch(new BasicGameEvent(
                GameEventType.LIVE_CONFIG_RELOADED,
                AbstractGameEvent.SYSTEM_ACTOR_ID,
                EventSource.ADMIN_GUI,
                Map.of("environment", environment, "entryCount", Integer.toString(entries.size()))
        )));
        return entries;
    }

    private void validateJson(String valueJson) {
        try {
            objectMapper.readTree(valueJson);
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Live config valueJson must be valid JSON.", ex);
        }
    }

    private void dispatchChangeEvent(LiveConfigEntry entry, Map<String, String> requestMetadata) {
        eventDispatchHandler.ifPresent(dispatcher -> {
            Map<String, String> attributes = new LinkedHashMap<>(requestMetadata);
            attributes.put("key", entry.key());
            attributes.put("type", entry.type().name());
            attributes.put("environment", entry.environment());
            attributes.put("enabled", Boolean.toString(entry.enabled()));
            attributes.put("version", Long.toString(entry.version()));
            dispatcher.dispatch(new BasicGameEvent(
                    eventTypeFor(entry),
                    actorId(entry.updatedBy()),
                    EventSource.ADMIN_GUI,
                    attributes
            ));
        });
    }

    private GameEventType eventTypeFor(LiveConfigEntry entry) {
        return switch (entry.type()) {
            case FEATURE_FLAG -> GameEventType.FEATURE_FLAG_CHANGED;
            case GAME_RULE, BALANCE_VALUE, SCHEDULED_MODIFIER -> GameEventType.GAME_RULE_CHANGED;
            case ITEM_TOGGLE -> entry.enabled() ? GameEventType.ITEM_UNVAULTED : GameEventType.ITEM_VAULTED;
            case SYSTEM_TOGGLE, RESOURCE_TOGGLE, EVENT_TOGGLE, COMMAND_TOGGLE -> GameEventType.SYSTEM_TOGGLE_CHANGED;
            case UI_DEFINITION -> GameEventType.GLOBAL_GUI_UPDATED;
        };
    }

    private UUID actorId(String updatedBy) {
        if (updatedBy == null || updatedBy.isBlank()) {
            return AbstractGameEvent.SYSTEM_ACTOR_ID;
        }
        try {
            return UUID.fromString(updatedBy);
        } catch (IllegalArgumentException ignored) {
            return UUID.nameUUIDFromBytes(("liveops:" + updatedBy).getBytes(StandardCharsets.UTF_8));
        }
    }
}
