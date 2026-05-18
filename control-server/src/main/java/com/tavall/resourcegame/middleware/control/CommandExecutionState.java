package org.tavall.control.runtime;

public enum CommandExecutionState {
    RECEIVED,
    VALIDATING,
    DRY_RUN_COMPLETED,
    EXECUTING,
    FANOUT_PENDING,
    FANOUT_COMPLETED,
    COMPLETED,
    PARTIALLY_COMPLETED,
    FAILED,
    REJECTED
}
