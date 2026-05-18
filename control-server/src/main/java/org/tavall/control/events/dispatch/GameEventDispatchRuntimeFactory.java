package org.tavall.control.events.dispatch;

public final class GameEventDispatchRuntimeFactory {
    private GameEventDispatchRuntimeFactory() {
    }

    public static GameEventDispatchRuntime createInMemoryRuntime() {
        return new GameEventDispatchRuntimeAssembler().createInMemoryRuntime();
    }
}
