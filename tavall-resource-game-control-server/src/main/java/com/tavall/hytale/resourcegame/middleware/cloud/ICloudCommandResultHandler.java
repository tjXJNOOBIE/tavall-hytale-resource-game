package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICloudCommandResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);

    String redact(String value);
}
