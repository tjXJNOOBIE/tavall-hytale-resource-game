package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.UUID;

@Controller
public class ControlPlayerController {
    private final ControlCommandRuntime runtime;
    private final WebControlHtmlHandler htmlHandler = new WebControlHtmlHandler();

    public ControlPlayerController(ControlCommandRuntime runtime) {
        this.runtime = runtime;
    }

    @GetMapping("/control/players")
    @ResponseBody
    public String players(@RequestParam Optional<String> universalPlayerId) {
        String body = "<form method=\"get\"><input name=\"universalPlayerId\" placeholder=\"universal player UUID\"><button>Lookup</button></form>";
        if (universalPlayerId.isPresent() && !universalPlayerId.orElseThrow().isBlank()) {
            UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(universalPlayerId.orElseThrow()));
            body += "<p>Account exists: " + runtime.accountRepository().findAccount(playerId).isPresent() + "</p>";
            body += "<p>Platform bindings: " + runtime.platformAccountBindingRepository().findPlatformBindings(playerId).size() + "</p>";
        }
        return htmlHandler.page("Players", body);
    }
}
