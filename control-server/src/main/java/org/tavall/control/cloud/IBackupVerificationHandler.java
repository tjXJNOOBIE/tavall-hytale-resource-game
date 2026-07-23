package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IBackupVerificationHandler extends IDependencyInjectableInterface {
    boolean verify(UUID backupId);
}
