package com.tavall.hytale.resourcegame.middleware.control;

import java.util.List;

public final class PlatformFanoutResultAggregator {
    public CommandExecutionState aggregateState(ControlCommandResult canonicalResult, List<PlatformCommandResult> platformResults) {
        if (!canonicalResult.success()) {
            return CommandExecutionState.FAILED;
        }
        if (platformResults.isEmpty() || platformResults.stream().allMatch(PlatformCommandResult::success)) {
            return CommandExecutionState.COMPLETED;
        }
        if (platformResults.stream().noneMatch(PlatformCommandResult::success)) {
            return CommandExecutionState.FAILED;
        }
        return CommandExecutionState.PARTIALLY_COMPLETED;
    }
}
