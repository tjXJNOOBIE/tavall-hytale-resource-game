package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;

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
