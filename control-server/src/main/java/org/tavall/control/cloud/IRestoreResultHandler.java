package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IRestoreResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);
}
