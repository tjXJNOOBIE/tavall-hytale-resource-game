package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;

public interface IWebControlKingdomViewHandler {
    String body(ControlCommandResult result);

    String borderSummary(UniversalKingdomSimulationSystem.KingdomId kingdomId);
}
