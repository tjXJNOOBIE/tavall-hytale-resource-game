package com.tavall.resourcegame.middleware.control;

public enum ControlPlatformFanoutRetryState {
    PENDING,
    RETRYING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
