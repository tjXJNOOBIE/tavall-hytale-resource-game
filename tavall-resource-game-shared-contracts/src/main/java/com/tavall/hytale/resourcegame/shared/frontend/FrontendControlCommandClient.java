package com.tavall.hytale.resourcegame.shared.frontend;

@FunctionalInterface
public interface FrontendControlCommandClient {
    FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope);
}
