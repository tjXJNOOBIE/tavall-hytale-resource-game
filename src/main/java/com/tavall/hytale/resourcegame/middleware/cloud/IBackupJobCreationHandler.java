package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface IBackupJobCreationHandler extends IDependencyInjectableInterface {
    BackupJob createAndCommand(BackupPlan plan, String sourcePath, UUID requestedBy, Instant now);
}
