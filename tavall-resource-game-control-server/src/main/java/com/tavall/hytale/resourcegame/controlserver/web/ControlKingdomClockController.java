package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockState;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomScheduleProjection;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class ControlKingdomClockController {
    private final WebControlHtmlHandler htmlHandler;
    private final WebControlPanelCommandHandler panelCommandHandler;
    private final ControlCommandRuntime runtime;

    public ControlKingdomClockController(ControlCommandRuntime runtime, ControlOperator webControlOperator) {
        this.runtime = runtime;
        this.htmlHandler = new WebControlHtmlHandler();
        this.panelCommandHandler = new WebControlPanelCommandHandler(new WebControlCommandSubmissionHandler(runtime, webControlOperator));
    }

    @GetMapping("/control/clock")
    @ResponseBody
    public String clock() {
        return htmlHandler.page("Kingdom Clock", body("kingdom-1", null));
    }

    @PostMapping("/control/clock/mode")
    @ResponseBody
    public String setMode(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String mode, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = panelCommandHandler.submitCommandLine("clock mode " + safeCommandText(kingdomId) + " " + safeCommandText(mode), dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    @PostMapping("/control/clock/override")
    @ResponseBody
    public String setOverride(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String time, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = panelCommandHandler.submitCommandLine("clock override " + safeCommandText(kingdomId) + " " + safeCommandText(time), dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    @PostMapping("/control/clock/clear-override")
    @ResponseBody
    public String clearOverride(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = panelCommandHandler.submitCommandLine("clock clear-override " + safeCommandText(kingdomId), dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    @PostMapping("/control/clock/tick")
    @ResponseBody
    public String tick(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = panelCommandHandler.submitCommandLine("clock tick " + safeCommandText(kingdomId), dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    @PostMapping("/control/clock/config")
    @ResponseBody
    public String updateConfig(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String parameterKey, @RequestParam String value, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = panelCommandHandler.submitCommandLine("clock config " + safeCommandText(kingdomId) + " " + safeCommandText(parameterKey) + "=" + safeCommandText(value), dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    @PostMapping("/control/clock/schedule")
    @ResponseBody
    public String createScheduleRule(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String ruleType, @RequestParam(defaultValue = "8") String startHour, @RequestParam(defaultValue = "17") String endHour, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "schedule create " + safeCommandText(kingdomId) + " " + safeCommandText(ruleType) + " startHour=" + safeCommandText(startHour) + " endHour=" + safeCommandText(endHour);
        ControlCommandResult result = panelCommandHandler.submitCommandLine(commandLine, dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    @PostMapping("/control/clock/aging")
    @ResponseBody
    public String updateAging(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam(defaultValue = "true") String enabled, @RequestParam(defaultValue = "60") String realMinutesPerAgeIncrement, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "aging policy " + safeCommandText(kingdomId) + " enabled=" + safeCommandText(enabled) + " realMinutesPerAgeIncrement=" + safeCommandText(realMinutesPerAgeIncrement);
        ControlCommandResult result = panelCommandHandler.submitCommandLine(commandLine, dryRun);
        return htmlHandler.page("Kingdom Clock", body(kingdomId, result));
    }

    private String body(String kingdomId, ControlCommandResult result) {
        KingdomClockState state = runtime.kingdomClockSystem().getCurrentClockState(kingdomId);
        KingdomClockProjection clockProjection = runtime.kingdomClockSystem().projectClockState(kingdomId, GamePlatform.PC);
        KingdomScheduleProjection scheduleProjection = runtime.kingdomClockSystem().projectScheduleState(kingdomId, GamePlatform.PC);
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>Dashboard</h2><table><tr><th>Kingdom</th><th>Day</th><th>Time</th><th>Phase</th><th>Mode</th><th>Timezone</th><th>Visual Mood</th><th>Active Rules</th></tr><tr><td>")
                .append(htmlHandler.escape(state.kingdomId())).append("</td><td>").append(state.currentKingdomDay()).append("</td><td>")
                .append(String.format("%02d:%02d", state.currentHour(), state.currentMinute())).append("</td><td>")
                .append(htmlHandler.escape(state.currentPhase().name())).append("</td><td>")
                .append(htmlHandler.escape(state.clockMode().name())).append("</td><td>")
                .append(htmlHandler.escape(state.timezoneId().orElse("UTC"))).append("</td><td>")
                .append(htmlHandler.escape(clockProjection.visualMood().name())).append("</td><td>")
                .append(clockProjection.activeScheduleRules().size()).append("</td></tr></table></section>");
        builder.append("<p>Canonical owner: ").append(htmlHandler.escape(clockProjection.metadata().get("canonicalOwner"))).append("</p>");
        builder.append("<section><h2>Controls</h2><form method=\"post\" action=\"/control/clock/mode\"><input name=\"kingdomId\" value=\"")
                .append(htmlHandler.escape(kingdomId)).append("\"><select name=\"mode\"><option>REAL_TIME_SYNCED</option><option>ACCELERATED</option><option>FIXED_OVERRIDE</option><option>PAUSED</option></select><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Set Mode</button></form>")
                .append("<form method=\"post\" action=\"/control/clock/override\"><input name=\"kingdomId\" value=\"").append(htmlHandler.escape(kingdomId)).append("\"><input name=\"time\" value=\"22:00\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Set Override</button></form>")
                .append("<form method=\"post\" action=\"/control/clock/clear-override\"><input name=\"kingdomId\" value=\"").append(htmlHandler.escape(kingdomId)).append("\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Clear Override</button></form>")
                .append("<form method=\"post\" action=\"/control/clock/tick\"><input name=\"kingdomId\" value=\"").append(htmlHandler.escape(kingdomId)).append("\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Run Tick</button></form></section>");
        builder.append("<section><h2>Config</h2><form method=\"post\" action=\"/control/clock/config\"><input name=\"kingdomId\" value=\"").append(htmlHandler.escape(kingdomId)).append("\"><input name=\"parameterKey\" value=\"acceleratedTimeMultiplier\"><input name=\"value\" value=\"12\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Apply</button></form></section>");
        builder.append("<section><h2>Schedule Rules</h2><form method=\"post\" action=\"/control/clock/schedule\"><input name=\"kingdomId\" value=\"").append(htmlHandler.escape(kingdomId)).append("\"><select name=\"ruleType\"><option>CITIZEN_JOB_SHIFT</option><option>SHOP_OPEN</option><option>BUILDING_PRODUCTIVITY</option><option>TROOP_TRAINING</option><option>NIGHT_EVENT_WINDOW</option></select><input name=\"startHour\" value=\"8\"><input name=\"endHour\" value=\"17\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Create</button></form><p>Active windows: ")
                .append(scheduleProjection.activeWindows().size()).append("</p></section>");
        builder.append("<section><h2>Aging Policy</h2><form method=\"post\" action=\"/control/clock/aging\"><input name=\"kingdomId\" value=\"").append(htmlHandler.escape(kingdomId)).append("\"><select name=\"enabled\"><option>true</option><option>false</option></select><input name=\"realMinutesPerAgeIncrement\" value=\"60\"><button name=\"dryRun\" value=\"true\">Dry Run</button><button name=\"dryRun\" value=\"false\">Apply</button></form></section>");
        if (result != null) {
            builder.append(htmlHandler.renderResult(result));
        }
        return builder.toString();
    }

    private String safeCommandText(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }
}
