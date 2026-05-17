package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.Map;

public interface IDiscordControlPlaneCommandBridge extends IDependencyInjectableInterface {
    FrontendCommandVerificationResult submitKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    );

    FrontendCommandVerificationResult submitAction(
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> actionArguments,
            String correlationId,
            Map<String, String> sourceMetadata
    );
}
