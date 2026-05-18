package org.tavall.control.cli;

import org.tavall.control.runtime.ControlCommandResult;

public interface IControlConsoleResultRenderer {
    String renderResult(ControlCommandResult result);
}
