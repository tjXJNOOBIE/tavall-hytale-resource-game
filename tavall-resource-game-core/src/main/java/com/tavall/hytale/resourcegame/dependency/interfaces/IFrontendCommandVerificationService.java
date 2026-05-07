package com.tavall.hytale.resourcegame.dependency.interfaces;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;

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
