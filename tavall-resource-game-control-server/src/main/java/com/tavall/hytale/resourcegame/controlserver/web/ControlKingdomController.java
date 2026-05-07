package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class ControlKingdomController {
    private final WebControlHtmlHandler htmlHandler;
    private final WebControlPanelCommandHandler panelCommandHandler;
    private final ControlCommandRuntime runtime;

    public ControlKingdomController(ControlCommandRuntime runtime, ControlOperator webControlOperator) {
        this.runtime = runtime;
        this.htmlHandler = new WebControlHtmlHandler();
        this.panelCommandHandler = new WebControlPanelCommandHandler(new WebControlCommandSubmissionHandler(runtime, webControlOperator));
    }

    @GetMapping("/control/kingdoms")
    @ResponseBody
    public String kingdoms() {
        return htmlHandler.page("Kingdoms", body(null));
    }

    @PostMapping("/control/kingdoms/create")
    @ResponseBody
    public String createKingdom(@RequestParam(defaultValue = "") String displayName, @RequestParam(defaultValue = "default") String worldId, @RequestParam(defaultValue = "1000") String borderSize, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "kingdom create --displayName \"" + safeCommandText(displayName) + "\" --worldId " + safeCommandText(worldId) + " --borderSize " + safeCommandText(borderSize);
        ControlCommandResult result = panelCommandHandler.submitCommandLine(commandLine, dryRun);
        return htmlHandler.page("Kingdoms", body(result));
    }

    @PostMapping("/control/kingdoms/parameters")
    @ResponseBody
    public String updateParameter(@RequestParam String kingdomId, @RequestParam String parameterKey, @RequestParam String value, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "params set " + safeCommandText(parameterKey) + " " + safeCommandText(value) + " KINGDOM " + safeCommandText(kingdomId);
        ControlCommandResult result = panelCommandHandler.submitCommandLine(commandLine, dryRun);
        return htmlHandler.page("Kingdoms", body(result));
    }

    @PostMapping("/control/kingdoms/border")
    @ResponseBody
    public String updateBorder(@RequestParam String kingdomId, @RequestParam String minX, @RequestParam String maxX, @RequestParam String minZ, @RequestParam String maxZ, @RequestParam(defaultValue = "default") String worldId, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "kingdom border update " + safeCommandText(kingdomId) + " " + safeCommandText(minX) + " " + safeCommandText(maxX) + " " + safeCommandText(minZ) + " " + safeCommandText(maxZ) + " " + safeCommandText(worldId);
        ControlCommandResult result = panelCommandHandler.submitCommandLine(commandLine, dryRun);
        return htmlHandler.page("Kingdoms", body(result));
    }

    private String body(ControlCommandResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>Create</h2><form method=\"post\" action=\"/control/kingdoms/create\">")
                .append("<input name=\"displayName\" placeholder=\"Kingdom display name\">")
                .append("<input name=\"worldId\" value=\"default\">")
                .append("<input name=\"borderSize\" value=\"1000\">")
                .append("<button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Create</button>")
                .append("</form></section>");
        builder.append("<section><h2>Dashboard</h2><table><tr><th>ID</th><th>Name</th><th>State</th><th>Folder</th><th>World</th><th>Players</th><th>Border</th></tr>");
        for (UniversalKingdomSimulationSystem.UniversalKingdom kingdom : runtime.kingdomSimulationSystem().repository().kingdoms()) {
            builder.append("<tr><td>").append(htmlHandler.escape(kingdom.kingdomId().value())).append("</td><td>")
                    .append(htmlHandler.escape(kingdom.displayName())).append("</td><td>")
                    .append(htmlHandler.escape(kingdom.state().name())).append("</td><td>")
                    .append(htmlHandler.escape(kingdom.folderName())).append("</td><td>")
                    .append(htmlHandler.escape(kingdom.worldId())).append("</td><td>")
                    .append(kingdom.populationStats().currentPlayers()).append("/").append(kingdom.editableParameters().maxActivePlayers()).append("</td><td>")
                    .append(borderSummary(kingdom.kingdomId())).append("</td></tr>");
        }
        builder.append("</table></section>");
        builder.append("<section><h2>Parameter Edit</h2><form method=\"post\" action=\"/control/kingdoms/parameters\">")
                .append("<input name=\"kingdomId\" placeholder=\"kingdom-1\"><input name=\"parameterKey\" value=\"maxActivePlayers\"><input name=\"value\" value=\"250\">")
                .append("<button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Apply</button></form></section>");
        builder.append("<section><h2>Border Editor</h2><form method=\"post\" action=\"/control/kingdoms/border\">")
                .append("<input name=\"kingdomId\" placeholder=\"kingdom-1\"><input name=\"worldId\" value=\"default\"><input name=\"minX\" value=\"0\"><input name=\"maxX\" value=\"1000\"><input name=\"minZ\" value=\"0\"><input name=\"maxZ\" value=\"1000\">")
                .append("<button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Apply</button></form></section>");
        if (result != null) {
            builder.append(htmlHandler.renderResult(result));
        }
        return builder.toString();
    }

    private String borderSummary(UniversalKingdomSimulationSystem.KingdomId kingdomId) {
        return runtime.kingdomSimulationSystem().repository().findBorderForKingdom(kingdomId)
                .map(border -> htmlHandler.escape(border.worldId() + " [" + border.minX() + "," + border.minZ() + "] -> [" + border.maxX() + "," + border.maxZ() + "]"))
                .orElse("none");
    }

    private String safeCommandText(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }
}
