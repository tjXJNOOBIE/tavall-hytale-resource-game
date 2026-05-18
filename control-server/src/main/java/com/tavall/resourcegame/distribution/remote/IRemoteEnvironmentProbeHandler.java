package org.tavall.control.distribution.remote;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IRemoteEnvironmentProbeHandler extends IDependencyInjectableInterface {
    RemoteEnvironmentSnapshot detectLocalEnvironment();

    RemoteEnvironmentSnapshot probeRemoteEnvironment(RemoteTarget target);
}
