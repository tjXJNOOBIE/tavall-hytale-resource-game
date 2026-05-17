package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface IBackupJobCreationHandler extends IDependencyInjectableInterface {
    BackupJob createAndCommand(BackupPlan plan, String sourcePath, UUID requestedBy, Instant now);
}
