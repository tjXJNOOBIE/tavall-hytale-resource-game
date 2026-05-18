package org.tavall.control.web;

import org.tavall.control.IControlServerDomain;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.kingdom.UniversalKingdomSimulationSystem;

public final class WebControlKingdomViewHandler implements IWebControlKingdomViewHandler, IControlServerDomain {
    public String body(ControlCommandResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>Create</h2><form method=\"post\" action=\"/control/kingdoms/create\">")
                .append("<input name=\"displayName\" placeholder=\"Kingdom display name\">")
                .append("<input name=\"worldId\" value=\"default\">")
                .append("<input name=\"borderSize\" value=\"1000\">")
                .append("<button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Create</button>")
                .append("</form></section>");
        builder.append("<section><h2>Dashboard</h2><table><tr><th>ID</th><th>Name</th><th>State</th><th>Folder</th><th>World</th><th>Players</th><th>Border</th></tr>");
        for (UniversalKingdomSimulationSystem.UniversalKingdom kingdom : getControlCommandRuntime().kingdomSimulationSystem().repository().kingdoms()) {
            builder.append("<tr><td>").append(getWebControlHtmlHandler().escape(kingdom.kingdomId().value())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(kingdom.displayName())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(kingdom.state().name())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(kingdom.folderName())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(kingdom.worldId())).append("</td><td>")
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
            builder.append(getWebControlHtmlHandler().renderResult(result));
        }
        return builder.toString();
    }

    public String borderSummary(UniversalKingdomSimulationSystem.KingdomId kingdomId) {
        return getControlCommandRuntime().kingdomSimulationSystem().repository().findBorderForKingdom(kingdomId)
                .map(border -> getWebControlHtmlHandler().escape(border.worldId() + " [" + border.minX() + "," + border.minZ() + "] -> [" + border.maxX() + "," + border.maxZ() + "]"))
                .orElse("none");
    }
}
