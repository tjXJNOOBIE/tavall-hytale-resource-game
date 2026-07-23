package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface ICloudCommandValidationHandler extends IDependencyInjectableInterface {
    void validate(CloudCommandType commandType, String payloadJson);
}
