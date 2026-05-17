package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICloudCommandValidationHandler extends IDependencyInjectableInterface {
    void validate(CloudCommandType commandType, String payloadJson);
}
