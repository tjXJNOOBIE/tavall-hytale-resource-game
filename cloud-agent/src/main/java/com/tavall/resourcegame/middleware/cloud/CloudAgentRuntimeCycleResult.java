package com.tavall.resourcegame.middleware.cloud;

public record CloudAgentRuntimeCycleResult(
        boolean heartbeatAccepted,
        int commandsReceived,
        int commandsExecuted,
        int resultsReported
) {
}
