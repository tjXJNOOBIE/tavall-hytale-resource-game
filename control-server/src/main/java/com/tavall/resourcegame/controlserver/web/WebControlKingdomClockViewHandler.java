package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.clock.KingdomClockProjection;
import com.tavall.resourcegame.middleware.clock.KingdomClockState;
import com.tavall.resourcegame.middleware.clock.KingdomScheduleProjection;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public final class WebControlKingdomClockViewHandler implements IWebControlKingdomClockViewHandler, IControlServerDomain {
    public String body(String kingdomId, ControlCommandResult result) {
        KingdomClockState state = getControlCommandRuntime().kingdomClockSystem().getCurrentClockState(kingdomId);
        KingdomClockProjection clockProjection = getControlCommandRuntime().kingdomClockSystem().projectClockState(kingdomId, GamePlatform.PC);
        KingdomScheduleProjection scheduleProjection = getControlCommandRuntime().kingdomClockSystem().projectScheduleState(kingdomId, GamePlatform.PC);
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>Dashboard</h2><table><tr><th>Kingdom</th><th>Day</th><th>Time</th><th>Phase</th><th>Mode</th><th>Timezone</th><th>Visual Mood</th><th>Active Rules</th></tr><tr><td>")
                .append(getWebControlHtmlHandler().escape(state.kingdomId())).append("</td><td>").append(state.currentKingdomDay()).append("</td><td>")
                .append(String.format("%02d:%02d", state.currentHour(), state.currentMinute())).append("</td><td>")
                .append(getWebControlHtmlHandler().escape(state.currentPhase().name())).append("</td><td>")
                .append(getWebControlHtmlHandler().escape(state.clockMode().name())).append("</td><td>")
                .append(getWebControlHtmlHandler().escape(state.timezoneId().orElse("UTC"))).append("</td><td>")
                .append(getWebControlHtmlHandler().escape(clockProjection.visualMood().name())).append("</td><td>")
                .append(clockProjection.activeScheduleRules().size()).append("</td></tr></table></section>");
        builder.append("<p>Canonical owner: ").append(getWebControlHtmlHandler().escape(clockProjection.metadata().get("canonicalOwner"))).append("</p>");
        builder.append("<section><h2>Controls</h2><form method=\"post\" action=\"/control/clock/mode\"><input name=\"kingdomId\" value=\"")
                .append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><select name=\"mode\"><option>REAL_TIME_SYNCED</option><option>ACCELERATED</option><option>FIXED_OVERRIDE</option><option>PAUSED</option></select><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Set Mode</button></form>")
                .append("<form method=\"post\" action=\"/control/clock/override\"><input name=\"kingdomId\" value=\"").append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><input name=\"time\" value=\"22:00\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Set Override</button></form>")
                .append("<form method=\"post\" action=\"/control/clock/clear-override\"><input name=\"kingdomId\" value=\"").append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Clear Override</button></form>")
                .append("<form method=\"post\" action=\"/control/clock/tick\"><input name=\"kingdomId\" value=\"").append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Run Tick</button></form></section>");
        builder.append("<section><h2>Config</h2><form method=\"post\" action=\"/control/clock/config\"><input name=\"kingdomId\" value=\"").append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><input name=\"parameterKey\" value=\"acceleratedTimeMultiplier\"><input name=\"value\" value=\"12\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Apply</button></form></section>");
        builder.append("<section><h2>Schedule Rules</h2><form method=\"post\" action=\"/control/clock/schedule\"><input name=\"kingdomId\" value=\"").append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><select name=\"ruleType\"><option>CITIZEN_JOB_SHIFT</option><option>SHOP_OPEN</option><option>BUILDING_PRODUCTIVITY</option><option>TROOP_TRAINING</option><option>NIGHT_EVENT_WINDOW</option></select><input name=\"startHour\" value=\"8\"><input name=\"endHour\" value=\"17\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Create</button></form><p>Active windows: ")
                .append(scheduleProjection.activeWindows().size()).append("</p></section>");
        builder.append("<section><h2>Aging Policy</h2><form method=\"post\" action=\"/control/clock/aging\"><input name=\"kingdomId\" value=\"").append(getWebControlHtmlHandler().escape(kingdomId)).append("\"><select name=\"enabled\"><option>true</option><option>false</option></select><input name=\"realMinutesPerAgeIncrement\" value=\"60\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Apply</button></form></section>");
        if (result != null) {
            builder.append(getWebControlHtmlHandler().renderResult(result));
        }
        return builder.toString();
    }
}
