package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.io.IOException;
import java.time.Instant;

public interface ICloudAgentRuntime extends IDependencyInjectableInterface {
    CloudAgentRuntimeCycleResult runOnce(Instant now) throws IOException, InterruptedException;

    void runForever() throws IOException, InterruptedException;
}
