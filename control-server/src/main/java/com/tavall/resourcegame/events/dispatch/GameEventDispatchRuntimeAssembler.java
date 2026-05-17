package com.tavall.resourcegame.events.dispatch;

import com.tavall.resourcegame.events.GameEventDependencyModule;
import com.tavall.resourcegame.events.IGameEventDomain;

public final class GameEventDispatchRuntimeAssembler implements IGameEventDomain {
    public GameEventDispatchRuntime createInMemoryRuntime() {
        new GameEventDependencyModule().registerDependencies();
        return new GameEventDispatchRuntime();
    }
}
