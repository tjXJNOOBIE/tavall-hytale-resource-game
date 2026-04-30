package com.tavall.hytale.resourcegame.middleware.control;

public enum ControlPlatformFanoutRetryState {
    PENDING,
    RETRYING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
