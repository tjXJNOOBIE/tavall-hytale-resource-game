package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IBackupResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);
}
