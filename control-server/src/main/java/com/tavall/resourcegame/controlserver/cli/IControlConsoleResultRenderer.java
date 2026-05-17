package com.tavall.resourcegame.controlserver.cli;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public interface IControlConsoleResultRenderer {
    String renderResult(ControlCommandResult result);
}
