package org.tavall.control.events.dispatch;

import org.tavall.control.events.GameEventDependencyModule;
import org.tavall.control.events.GameEventDomain;

public final class GameEventDispatchRuntimeAssembler implements GameEventDomain {
    public GameEventDispatchRuntime createInMemoryRuntime() {
        new GameEventDependencyModule().registerDependencies();
        return new GameEventDispatchRuntime();
    }
}
