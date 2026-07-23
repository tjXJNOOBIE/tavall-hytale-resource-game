package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IBackupResultHandler extends IDependencyInjectableInterface {
    boolean record(CloudCommandResult result);
}
