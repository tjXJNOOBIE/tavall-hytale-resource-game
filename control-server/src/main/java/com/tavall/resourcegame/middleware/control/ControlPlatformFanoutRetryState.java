package org.tavall.control.runtime;

public enum ControlPlatformFanoutRetryState {
    PENDING,
    RETRYING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
