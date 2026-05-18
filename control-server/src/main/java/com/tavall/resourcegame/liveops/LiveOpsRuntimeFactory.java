package org.tavall.control.liveops;

import org.tavall.control.events.dispatch.GameEventDispatchHandler;

public final class LiveOpsRuntimeFactory {
    private LiveOpsRuntimeFactory() {
    }

    public static LiveOpsRuntime createInMemoryRuntime() {
        return new LiveOpsRuntimeAssembler().createInMemoryRuntime();
    }

    public static LiveOpsRuntime createInMemoryRuntime(GameEventDispatchHandler eventDispatchHandler) {
        return createInMemoryRuntime();
    }
}
