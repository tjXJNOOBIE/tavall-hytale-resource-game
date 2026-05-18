package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.tavall.control.runtime.ControlCommandResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class ControlKingdomController implements ControlServerDomain {
    @GetMapping("/control/kingdoms")
    @ResponseBody
    public String kingdoms() {
        return getWebControlHtmlHandler().page("Kingdoms", getWebControlKingdomViewHandler().body(null));
    }

    @PostMapping("/control/kingdoms/create")
    @ResponseBody
    public String createKingdom(@RequestParam(defaultValue = "") String displayName, @RequestParam(defaultValue = "default") String worldId, @RequestParam(defaultValue = "1000") String borderSize, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "kingdom create --displayName \"" + safeCommandText(displayName) + "\" --worldId " + safeCommandText(worldId) + " --borderSize " + safeCommandText(borderSize);
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine(commandLine, dryRun);
        return getWebControlHtmlHandler().page("Kingdoms", getWebControlKingdomViewHandler().body(result));
    }

    @PostMapping("/control/kingdoms/parameters")
    @ResponseBody
    public String updateParameter(@RequestParam String kingdomId, @RequestParam String parameterKey, @RequestParam String value, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "params set " + safeCommandText(parameterKey) + " " + safeCommandText(value) + " KINGDOM " + safeCommandText(kingdomId);
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine(commandLine, dryRun);
        return getWebControlHtmlHandler().page("Kingdoms", getWebControlKingdomViewHandler().body(result));
    }

    @PostMapping("/control/kingdoms/border")
    @ResponseBody
    public String updateBorder(@RequestParam String kingdomId, @RequestParam String minX, @RequestParam String maxX, @RequestParam String minZ, @RequestParam String maxZ, @RequestParam(defaultValue = "default") String worldId, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "kingdom border update " + safeCommandText(kingdomId) + " " + safeCommandText(minX) + " " + safeCommandText(maxX) + " " + safeCommandText(minZ) + " " + safeCommandText(maxZ) + " " + safeCommandText(worldId);
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine(commandLine, dryRun);
        return getWebControlHtmlHandler().page("Kingdoms", getWebControlKingdomViewHandler().body(result));
    }

    private String safeCommandText(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }
}
