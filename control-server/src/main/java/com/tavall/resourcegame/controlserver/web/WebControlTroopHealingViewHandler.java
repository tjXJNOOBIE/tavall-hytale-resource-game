package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.healing.TroopHealingPlan;
import com.tavall.resourcegame.middleware.healing.TroopWound;
import com.tavall.resourcegame.middleware.troop.TroopId;

import java.util.Optional;
import java.util.UUID;

public final class WebControlTroopHealingViewHandler implements IWebControlTroopHealingViewHandler, IControlServerDomain {
    public String troopHealingBody(Optional<String> troopId) {
        StringBuilder body = new StringBuilder("<form method=\"get\"><input name=\"troopId\" placeholder=\"troop UUID\"><button>Lookup</button></form>");
        troopId.filter(value -> !value.isBlank()).ifPresent(value -> {
            TroopId parsedTroopId = new TroopId(UUID.fromString(value));
            body.append("<h2>Wounds</h2><ul>");
            for (TroopWound wound : getControlCommandRuntime().troopHealingRepository().findActiveWoundsForTroop(parsedTroopId)) {
                body.append("<li>").append(getWebControlHtmlHandler().escape(wound.woundType().name())).append(" ").append(getWebControlHtmlHandler().escape(wound.severity().name())).append("</li>");
            }
            body.append("</ul><h2>Active Plan</h2>");
            body.append(getControlCommandRuntime().troopHealingRepository().findActiveHealingPlanForTroop(parsedTroopId)
                    .map(TroopHealingPlan::healingPlanId)
                    .map(Object::toString)
                    .map(getWebControlHtmlHandler()::escape)
                    .orElse("none"));
        });
        return body.toString();
    }
}
