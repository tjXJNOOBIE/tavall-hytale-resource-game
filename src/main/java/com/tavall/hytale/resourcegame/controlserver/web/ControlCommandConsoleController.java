package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlCommandConsoleController {
    private final WebControlHtmlHandler htmlHandler;
    private final WebControlPanelCommandHandler panelCommandHandler;

    public ControlCommandConsoleController(ControlCommandRuntime runtime, ControlOperator webControlOperator) {
        this.htmlHandler = new WebControlHtmlHandler();
        this.panelCommandHandler = new WebControlPanelCommandHandler(new WebControlCommandSubmissionHandler(runtime, webControlOperator));
    }

    @GetMapping("/control/commands")
    @ResponseBody
    public String commandConsole() {
        String body = "<form method=\"post\" action=\"/control/commands\"><textarea name=\"commandLine\">dry-run projection refresh</textarea><br><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Execute</button></form>";
        return htmlHandler.page("Command Console", body);
    }

    @PostMapping("/control/commands")
    @ResponseBody
    public String submitCommand(@RequestParam String commandLine, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = panelCommandHandler.submitCommandLine(commandLine, dryRun);
        String body = "<form method=\"post\" action=\"/control/commands\"><textarea name=\"commandLine\">" + htmlHandler.escape(commandLine) + "</textarea><br><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Execute</button></form>"
                + htmlHandler.renderResult(result);
        return htmlHandler.page("Command Console", body);
    }
}
