package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IBackupVerificationHandler extends IDependencyInjectableInterface {
    boolean verify(UUID backupId);
}
