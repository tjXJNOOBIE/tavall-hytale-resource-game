package com.tavall.resourcegame.liveops;

public final class LiveOpsRuntimeAssembler implements ILiveOpsDomain {
    public LiveOpsRuntime createInMemoryRuntime() {
        new LiveOpsDependencyModule().registerDependencies();
        return new LiveOpsRuntime();
    }
}
