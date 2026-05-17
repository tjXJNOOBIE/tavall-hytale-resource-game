package com.tavall.resourcegame.middleware.cloud;

public enum BackupType {
    POSTGRES_DUMP,
    REDIS_SNAPSHOT,
    WORLD_FOLDER,
    CONFIG_FOLDER,
    FULL_WORKLOAD,
    AUDIT_LOG,
    CUSTOM
}
