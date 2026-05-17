package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICloudCommandResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);

    String redact(String value);
}
