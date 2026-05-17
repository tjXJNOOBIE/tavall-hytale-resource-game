package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.Map;

public interface IRobloxFrontendCommandEnvelopeFactory extends IDependencyInjectableInterface {
    FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    );

    FrontendCommandEnvelope actionEnvelope(
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> actionArguments,
            String correlationId,
            Map<String, String> sourceMetadata
    );
}
