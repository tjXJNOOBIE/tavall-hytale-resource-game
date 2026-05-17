package com.tavall.resourcegame.events.dispatch;

import com.tavall.resourcegame.events.core.GameEventType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GameEventListenerRegistry {
    private final Map<GameEventType, List<GameEventListener>> listenersByType = new EnumMap<>(GameEventType.class);
    private final List<GameEventListener> globalListeners = new ArrayList<>();

    public synchronized void register(GameEventType eventType, GameEventListener listener) {
        listenersByType.computeIfAbsent(eventType, ignored -> new ArrayList<>()).add(listener);
    }

    public synchronized void registerGlobal(GameEventListener listener) {
        globalListeners.add(listener);
    }

    public synchronized List<GameEventListener> listenersFor(GameEventType eventType) {
        List<GameEventListener> listeners = new ArrayList<>(globalListeners);
        listeners.addAll(listenersByType.getOrDefault(eventType, List.of()));
        return List.copyOf(listeners);
    }
}
