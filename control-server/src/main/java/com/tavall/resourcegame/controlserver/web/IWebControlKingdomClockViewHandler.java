package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public interface IWebControlKingdomClockViewHandler {
    String body(String kingdomId, ControlCommandResult result);
}
