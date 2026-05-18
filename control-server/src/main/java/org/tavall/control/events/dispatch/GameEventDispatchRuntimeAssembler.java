package org.tavall.control.events.dispatch;

import org.tavall.control.events.GameEventDependencyModule;
import org.tavall.control.events.IGameEventDomain;

public final class GameEventDispatchRuntimeAssembler implements IGameEventDomain {
    public GameEventDispatchRuntime createInMemoryRuntime() {
        new GameEventDependencyModule().registerDependencies();
        return new GameEventDispatchRuntime();
    }
}
