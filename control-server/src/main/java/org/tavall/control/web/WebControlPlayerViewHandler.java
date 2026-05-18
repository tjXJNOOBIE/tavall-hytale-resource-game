package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.tavall.control.identity.UniversalPlayerId;

import java.util.Optional;
import java.util.UUID;

public final class WebControlPlayerViewHandler implements IWebControlPlayerViewHandler, ControlServerDomain {
    public String playersBody(Optional<String> universalPlayerId) {
        String body = "<form method=\"get\"><input name=\"universalPlayerId\" placeholder=\"universal player UUID\"><button>Lookup</button></form>";
        if (universalPlayerId.isPresent() && !universalPlayerId.orElseThrow().isBlank()) {
            UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(universalPlayerId.orElseThrow()));
            body += "<p>Account exists: " + getControlCommandRuntime().accountRepository().findAccount(playerId).isPresent() + "</p>";
            body += "<p>Platform bindings: " + getControlCommandRuntime().platformAccountBindingRepository().findPlatformBindings(playerId).size() + "</p>";
        }
        return body;
    }
}
