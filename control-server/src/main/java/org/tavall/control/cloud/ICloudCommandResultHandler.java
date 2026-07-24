package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface ICloudCommandResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);

    String redact(String value);
}
