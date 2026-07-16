package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;

import java.util.List;
import java.util.Map;

public interface IFrontendCommandVerificationHandler extends IDependencyInjectableInterface {
    FrontendCommandVerificationResult verifyMinecraftKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            Map<String, String> sourceMetadata
    );
}

