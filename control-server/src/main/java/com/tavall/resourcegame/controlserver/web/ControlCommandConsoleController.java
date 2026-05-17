package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.control.ControlCommandResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlCommandConsoleController implements IControlServerDomain {
    @GetMapping("/control/commands")
    @ResponseBody
    public String commandConsole() {
        String body = "<form method=\"post\" action=\"/control/commands\"><textarea name=\"commandLine\">dry-run projection refresh</textarea><br><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Execute</button></form>";
        return getWebControlHtmlHandler().page("Command Console", body);
    }

    @PostMapping("/control/commands")
    @ResponseBody
    public String submitCommand(@RequestParam String commandLine, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine(commandLine, dryRun);
        String body = "<form method=\"post\" action=\"/control/commands\"><textarea name=\"commandLine\">" + getWebControlHtmlHandler().escape(commandLine) + "</textarea><br><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Execute</button></form>"
                + getWebControlHtmlHandler().renderResult(result);
        return getWebControlHtmlHandler().page("Command Console", body);
    }
}
