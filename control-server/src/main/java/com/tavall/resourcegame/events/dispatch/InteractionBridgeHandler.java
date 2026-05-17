package com.tavall.resourcegame.events.dispatch;

import com.tavall.resourcegame.events.IGameEventDomain;
import com.tavall.resourcegame.events.core.BasicGameEvent;
import com.tavall.resourcegame.events.core.EventSource;
import com.tavall.resourcegame.events.core.GameEvent;
import com.tavall.resourcegame.events.core.GameEventResult;
import com.tavall.resourcegame.events.core.GameEventType;

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
