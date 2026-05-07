package com.tavall.hytale.resourcegame.frontend.hytale;

import java.net.URI;
import java.util.Map;

public final class HytaleFrontendConfig {
    private final URI controlIngressUri;
    private final String serverId;

    public HytaleFrontendConfig(URI controlIngressUri, String serverId) {
        this.controlIngressUri = controlIngressUri;
        this.serverId = serverId;
    }

    /**
     * Allows a Hytale-only override while keeping the shared control-ingress variable usable
     * across Minecraft, Discord, and local tooling.
     */
    public static HytaleFrontendConfig fromEnvironment(Map<String, String> environment) {
        return new HytaleFrontendConfig(
                URI.create(firstNonBlank(
                        environment.get("RESOURCE_GAME_HYTALE_CONTROL_INGRESS_URL"),
                        environment.get("RESOURCE_GAME_CONTROL_INGRESS_URL"),
                        "http://127.0.0.1:8080/api/frontend/commands"
                )),
                firstNonBlank(environment.get("RESOURCE_GAME_HYTALE_SERVER_ID"), "hytale-single-server")
        );
    }

    public URI controlIngressUri() {
        return controlIngressUri;
    }

    public String serverId() {
        return serverId;
    }

    private static String firstNonBlank(String firstCandidate, String secondCandidate, String fallback) {
        String firstResolved = firstNonBlank(firstCandidate, "");
        return firstResolved.isBlank() ? firstNonBlank(secondCandidate, fallback) : firstResolved;
    }

    private static String firstNonBlank(String candidate, String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }
}
