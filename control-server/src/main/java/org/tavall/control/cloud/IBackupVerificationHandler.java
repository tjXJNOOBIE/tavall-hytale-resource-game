package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IBackupVerificationHandler extends IDependencyInjectableInterface {
    boolean verify(UUID backupId);
}
