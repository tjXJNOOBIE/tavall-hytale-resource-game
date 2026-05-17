package com.tavall.resourcegame.middleware.cloud;

public enum WorkloadActualState {
    UNKNOWN,
    PENDING,
    INSTALLING,
    RUNNING,
    STOPPED,
    RESTARTING,
    DRAINING,
    MIGRATING,
    FAILED,
    DELETED
}
