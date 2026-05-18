package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICloudCommandResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);

    String redact(String value);
}
