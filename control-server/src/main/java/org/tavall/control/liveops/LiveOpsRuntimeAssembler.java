package org.tavall.control.liveops;

public final class LiveOpsRuntimeAssembler implements LiveOpsDomain {
    public LiveOpsRuntime createInMemoryRuntime() {
        new LiveOpsDependencyModule().registerDependencies();
        return new LiveOpsRuntime();
    }
}
