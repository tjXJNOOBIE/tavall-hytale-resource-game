package org.tavall.control.events.dispatch;

import org.tavall.control.events.IGameEventDomain;
import org.tavall.control.events.core.BasicGameEvent;
import org.tavall.control.events.core.EventSource;
import org.tavall.control.events.core.GameEvent;
import org.tavall.control.events.core.GameEventResult;
import org.tavall.control.events.core.GameEventType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InteractionBridgeHandler implements IGameEventDomain {
    public GameEvent translate(FrontendInteractionRequest request) {
        GameEventType eventType = getEventInteractionMappingRegistry().eventTypeFor(request.actionId());
        Map<String, String> attributes = new LinkedHashMap<>(request.payload());
        attributes.put("interactionId", request.interactionId().toString());
        attributes.put("actionId", request.actionId());
        attributes.put("targetId", request.targetId());
        attributes.put("occurredAt", request.occurredAt().toString());
        return new BasicGameEvent(eventType, request.actorId(), EventSource.FRONTEND_INTERACTION, attributes);
    }

    public GameEventResult submitInteraction(FrontendInteractionRequest request) {
        return getGameEventDispatchHandler().dispatch(translate(request));
    }
}
