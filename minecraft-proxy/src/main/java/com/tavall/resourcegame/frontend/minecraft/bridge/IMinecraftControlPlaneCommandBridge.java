package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.Map;

public interface IMinecraftControlPlaneCommandBridge extends IDependencyInjectableInterface {
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
