package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IRestoreResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);
}
