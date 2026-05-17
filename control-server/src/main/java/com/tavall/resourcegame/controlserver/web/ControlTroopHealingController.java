package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public final class ControlTroopHealingController implements IControlServerDomain {
    @GetMapping("/control/healing")
    @ResponseBody
    public String troopHealing(@RequestParam Optional<String> troopId) {
        return getWebControlHtmlHandler().page("Troop Healing", getWebControlTroopHealingViewHandler().troopHealingBody(troopId));
    }
}
