package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;

import java.util.List;
import java.util.Map;

public interface IFrontendCommandVerificationService extends IDependencyInjectableInterface {
    FrontendCommandVerificationResult verifyHytaleKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            Map<String, String> sourceMetadata
    );
}
