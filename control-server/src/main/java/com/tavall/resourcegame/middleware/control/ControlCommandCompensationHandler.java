package org.tavall.control.runtime;

import java.util.Map;

public final class ControlCommandCompensationHandler {
    public ControlCommandCompensationDecision evaluateResult(ControlCommand command, ControlCommandResult result) {
        if (result.state() == CommandExecutionState.PARTIALLY_COMPLETED) {
            return new ControlCommandCompensationDecision(
                    result.commandId(),
                    false,
                    true,
                    "Canonical state completed but one or more platform fanout targets failed; retry fanout before considering gameplay compensation.",
                    Map.of("commandType", command.commandType().name())
            );
        }
        if (!result.success() && command.commandType().name().startsWith("DEBUG")) {
            return new ControlCommandCompensationDecision(result.commandId(), false, false, "Debug command failed without canonical mutation.", Map.of());
        }
        if (!result.success()) {
            return new ControlCommandCompensationDecision(result.commandId(), false, true, "Command failed before compensation rules were available for this command type.", Map.of("commandType", command.commandType().name()));
        }
        return new ControlCommandCompensationDecision(result.commandId(), false, false, "No compensation required.", Map.of("commandType", command.commandType().name()));
    }
}
