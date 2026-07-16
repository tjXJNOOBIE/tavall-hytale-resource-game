package org.tavall.control.liveops.gui;

import org.tavall.control.events.core.AbstractGameEvent;
import org.tavall.control.events.core.BasicGameEvent;
import org.tavall.control.events.core.EventSource;
import org.tavall.control.events.core.GameEventType;
import org.tavall.control.liveops.LiveOpsDomain;
import org.tavall.control.liveops.config.LiveConfigValidationException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GlobalGuiMutationHandler implements LiveOpsDomain {
    public GlobalGuiDefinition applyChange(GlobalGuiChangeRequest request) {
        return applyChange(request, Instant.now());
    }

    public GlobalGuiDefinition applyChange(GlobalGuiChangeRequest request, Instant now) {
        validateLayout(request.layoutJson());
        GlobalGuiDefinition definition = getGlobalGuiRepository().findByKey(request.guiKey())
                .map(existing -> existing.withVersionedLayout(request.title(), request.layoutJson(), request.enabled(), request.updatedBy(), now))
                .orElseGet(() -> new GlobalGuiDefinition(UUID.randomUUID(), request.guiKey(), request.title(), request.layoutJson(), 1L, request.enabled(), now, request.updatedBy()));
        getGlobalGuiRepository().save(definition);
        getGlobalGuiRegistry().apply(definition);
        GlobalGuiChange change = new GlobalGuiChange(definition, now, request.metadata());
        getGlobalGuiChangePublisher().publish(change);
        dispatchUpdateEvent(definition, request.metadata());
        return definition;
    }

    public List<GlobalGuiDefinition> reloadEnabled() {
        List<GlobalGuiDefinition> definitions = getGlobalGuiRepository().findEnabled();
        getGlobalGuiRegistry().reload(definitions);
        return definitions;
    }

    private void validateLayout(String layoutJson) {
        try {
            getLiveOpsObjectMapper().readTree(layoutJson);
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Global GUI layoutJson must be valid JSON.", ex);
        }
    }

    private void dispatchUpdateEvent(GlobalGuiDefinition definition, Map<String, String> requestMetadata) {
        getOptionalGameEventDispatchHandler().ifPresent(dispatcher -> {
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
