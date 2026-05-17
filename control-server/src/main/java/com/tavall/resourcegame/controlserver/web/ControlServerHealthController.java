package com.tavall.resourcegame.controlserver.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public final class ControlServerHealthController {
    /**
     * Liveness only: game runtimes use this before submitting commands, so it must not depend on Redis/Postgres.
     */
    @GetMapping({"/health", "/actuator/health", "/api/frontend/health"})
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "resource-game-control-server",
                "checkedAt", Instant.now().toString()
        );
    }
}
