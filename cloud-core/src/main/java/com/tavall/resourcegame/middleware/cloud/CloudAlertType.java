package com.tavall.resourcegame.middleware.cloud;

public enum CloudAlertType {
    NODE_MISSED_HEARTBEAT,
    NODE_DISK_HIGH,
    NODE_RAM_HIGH,
    WORKLOAD_CRASH_LOOP,
    DATABASE_UNREACHABLE,
    BACKUP_FAILED,
    REMOTE_COMMAND_FAILED,
    AGENT_VERSION_STALE,
    PROXY_UNREACHABLE
}
