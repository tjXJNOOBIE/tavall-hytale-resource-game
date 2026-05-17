package com.tavall.resourcegame.controlserver.web;

import java.util.Optional;

public interface IWebControlTroopHealingViewHandler {
    String troopHealingBody(Optional<String> troopId);
}
