package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ISystemdWorkloadRuntimeAdapter extends IDependencyInjectableInterface {
    CloudCommandResult execute(CloudCommand command, Instant now);
}
