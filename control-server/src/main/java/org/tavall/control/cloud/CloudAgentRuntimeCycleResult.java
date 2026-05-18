package org.tavall.control.cloud;

public record CloudAgentRuntimeCycleResult(
        boolean heartbeatAccepted,
        int commandsReceived,
        int commandsExecuted,
        int resultsReported
) {
}
