package com.tavall.resourcegame.middleware.cloud;

public enum CloudCommandStatus {
    CREATED,
    QUEUED,
    SENT,
    ACKNOWLEDGED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    EXPIRED,
    CANCELLED,
    REJECTED
}
