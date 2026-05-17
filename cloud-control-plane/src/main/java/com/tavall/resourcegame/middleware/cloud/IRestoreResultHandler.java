package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IRestoreResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);
}
