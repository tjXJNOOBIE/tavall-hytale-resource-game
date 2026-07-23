package org.tavall.control.distribution.remote;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IRemoteEnvironmentProbeHandler extends IDependencyInjectableInterface {
    RemoteEnvironmentSnapshot detectLocalEnvironment();

    RemoteEnvironmentSnapshot probeRemoteEnvironment(RemoteTarget target);
}
