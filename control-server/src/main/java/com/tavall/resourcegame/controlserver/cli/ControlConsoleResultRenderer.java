package com.tavall.resourcegame.controlserver.cli;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.resourcegame.middleware.control.PlatformCommandResult;

public final class ControlConsoleResultRenderer implements IControlConsoleResultRenderer {
    public String renderResult(ControlCommandResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("commandId=").append(result.commandId()).append(System.lineSeparator());
        builder.append("state=").append(result.state()).append(System.lineSeparator());
        builder.append("success=").append(result.success()).append(System.lineSeparator());
        builder.append("message=").append(result.message()).append(System.lineSeparator());
        if (!result.changedObjectIds().isEmpty()) {
            builder.append("changedObjects=").append(String.join(",", result.changedObjectIds())).append(System.lineSeparator());
        }
        for (PlatformCommandResult platformResult : result.platformResults()) {
            builder.append("platform.")
                    .append(platformResult.platform())
                    .append("=")
                    .append(platformResult.success())
                    .append(" ")
                    .append(platformResult.message())
                    .append(System.lineSeparator());
        }
        if (!result.validationErrors().isEmpty()) {
            builder.append("validationErrors=").append(String.join(" | ", result.validationErrors())).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
