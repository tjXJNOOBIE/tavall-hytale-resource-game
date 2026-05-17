package com.tavall.resourcegame.liveops;

import com.tavall.resourcegame.events.dispatch.GameEventDispatchHandler;

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
