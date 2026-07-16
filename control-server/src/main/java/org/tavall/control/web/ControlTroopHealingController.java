package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public final class ControlTroopHealingController implements ControlServerDomain {
    @GetMapping("/control/healing")
    @ResponseBody
    public String troopHealing(@RequestParam Optional<String> troopId) {
        return getWebControlHtmlHandler().page("Troop Healing", getWebControlTroopHealingViewHandler().troopHealingBody(troopId));
    }
}
