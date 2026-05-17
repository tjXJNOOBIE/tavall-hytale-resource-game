package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

import java.util.Map;

public record CloudAgentSecurityConfig(
        boolean commandSigningRequired,
        String commandSigningSecret
) implements IDependencyInjectableConcrete {
    public CloudAgentSecurityConfig {
        if (commandSigningSecret != null && commandSigningSecret.isBlank()) {
            commandSigningSecret = null;
        }
        commandSigningRequired = commandSigningRequired && commandSigningSecret != null;
    }

    public static CloudAgentSecurityConfig fromEnvironment(Map<String, String> environment) {
        String secret = environment.get("TAVALL_CLOUD_AGENT_COMMAND_SIGNING_SECRET");
        return new CloudAgentSecurityConfig(secret != null && !secret.isBlank(), secret);
    }
}
