package com.tavall.hytale.resourcegame.middleware.control;

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
