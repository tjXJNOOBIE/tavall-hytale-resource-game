package com.tavall.resourcegame.controlserver.web;

import java.util.Optional;

public interface IWebControlPlayerViewHandler {
    String playersBody(Optional<String> universalPlayerId);
}
