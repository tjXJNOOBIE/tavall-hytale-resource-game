package org.tavall.control.liveops;

public final class LiveOpsRuntimeAssembler implements ILiveOpsDomain {
    public LiveOpsRuntime createInMemoryRuntime() {
        new LiveOpsDependencyModule().registerDependencies();
        return new LiveOpsRuntime();
    }
}
