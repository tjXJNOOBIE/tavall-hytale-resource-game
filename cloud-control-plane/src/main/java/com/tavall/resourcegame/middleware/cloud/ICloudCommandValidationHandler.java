package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICloudCommandValidationHandler extends IDependencyInjectableInterface {
    void validate(CloudCommandType commandType, String payloadJson);
}
