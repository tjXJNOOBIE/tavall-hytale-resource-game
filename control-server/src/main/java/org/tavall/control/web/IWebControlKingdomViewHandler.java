package org.tavall.control.web;

import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.kingdom.UniversalKingdomSimulationSystem;

public interface IWebControlKingdomViewHandler {
    String body(ControlCommandResult result);

    String borderSummary(UniversalKingdomSimulationSystem.KingdomId kingdomId);
}
