package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IRawProcessWorkloadRuntimeAdapter extends IDependencyInjectableInterface {
    CloudCommandResult execute(CloudCommand command, Instant now);
}
