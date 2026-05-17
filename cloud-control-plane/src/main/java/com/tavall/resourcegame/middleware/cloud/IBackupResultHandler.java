package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IBackupResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);
}
