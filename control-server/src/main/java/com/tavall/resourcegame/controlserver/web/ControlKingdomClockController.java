package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.control.ControlCommandResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class ControlKingdomClockController implements IControlServerDomain {
    @GetMapping("/control/clock")
    @ResponseBody
    public String clock() {
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body("kingdom-1", null));
    }

    @PostMapping("/control/clock/mode")
    @ResponseBody
    public String setMode(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String mode, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine("clock mode " + safeCommandText(kingdomId) + " " + safeCommandText(mode), dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    @PostMapping("/control/clock/override")
    @ResponseBody
    public String setOverride(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String time, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine("clock override " + safeCommandText(kingdomId) + " " + safeCommandText(time), dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    @PostMapping("/control/clock/clear-override")
    @ResponseBody
    public String clearOverride(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine("clock clear-override " + safeCommandText(kingdomId), dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    @PostMapping("/control/clock/tick")
    @ResponseBody
    public String tick(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine("clock tick " + safeCommandText(kingdomId), dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    @PostMapping("/control/clock/config")
    @ResponseBody
    public String updateConfig(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String parameterKey, @RequestParam String value, @RequestParam(defaultValue = "false") boolean dryRun) {
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine("clock config " + safeCommandText(kingdomId) + " " + safeCommandText(parameterKey) + "=" + safeCommandText(value), dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    @PostMapping("/control/clock/schedule")
    @ResponseBody
    public String createScheduleRule(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam String ruleType, @RequestParam(defaultValue = "8") String startHour, @RequestParam(defaultValue = "17") String endHour, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "schedule create " + safeCommandText(kingdomId) + " " + safeCommandText(ruleType) + " startHour=" + safeCommandText(startHour) + " endHour=" + safeCommandText(endHour);
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine(commandLine, dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    @PostMapping("/control/clock/aging")
    @ResponseBody
    public String updateAging(@RequestParam(defaultValue = "kingdom-1") String kingdomId, @RequestParam(defaultValue = "true") String enabled, @RequestParam(defaultValue = "60") String realMinutesPerAgeIncrement, @RequestParam(defaultValue = "false") boolean dryRun) {
        String commandLine = "aging policy " + safeCommandText(kingdomId) + " enabled=" + safeCommandText(enabled) + " realMinutesPerAgeIncrement=" + safeCommandText(realMinutesPerAgeIncrement);
        ControlCommandResult result = getWebControlPanelCommandHandler().submitCommandLine(commandLine, dryRun);
        return getWebControlHtmlHandler().page("Kingdom Clock", getWebControlKingdomClockViewHandler().body(kingdomId, result));
    }

    private String safeCommandText(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }
}
