package org.tavall.control.cloud;

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
