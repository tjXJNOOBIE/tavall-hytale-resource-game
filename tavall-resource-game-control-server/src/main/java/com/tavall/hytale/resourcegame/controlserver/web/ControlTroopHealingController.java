package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingPlan;
import com.tavall.hytale.resourcegame.middleware.healing.TroopWound;
import com.tavall.hytale.resourcegame.middleware.troop.TroopId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.UUID;

@Controller
public class ControlTroopHealingController {
    private final ControlCommandRuntime runtime;
    private final WebControlHtmlHandler htmlHandler;

    public ControlTroopHealingController(ControlCommandRuntime runtime) {
        this.runtime = runtime;
        this.htmlHandler = new WebControlHtmlHandler();
    }

    @GetMapping("/control/healing")
    @ResponseBody
    public String troopHealing(@RequestParam Optional<String> troopId) {
        StringBuilder body = new StringBuilder("<form method=\"get\"><input name=\"troopId\" placeholder=\"troop UUID\"><button>Lookup</button></form>");
        troopId.filter(value -> !value.isBlank()).ifPresent(value -> {
            TroopId parsedTroopId = new TroopId(UUID.fromString(value));
            body.append("<h2>Wounds</h2><ul>");
            for (TroopWound wound : runtime.troopHealingRepository().findActiveWoundsForTroop(parsedTroopId)) {
                body.append("<li>").append(htmlHandler.escape(wound.woundType().name())).append(" ").append(htmlHandler.escape(wound.severity().name())).append("</li>");
            }
            body.append("</ul><h2>Active Plan</h2>");
            body.append(runtime.troopHealingRepository().findActiveHealingPlanForTroop(parsedTroopId)
                    .map(TroopHealingPlan::healingPlanId)
                    .map(Object::toString)
                    .map(htmlHandler::escape)
                    .orElse("none"));
        });
        return htmlHandler.page("Troop Healing", body.toString());
    }
}
