package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

/**
 * Roblox Luau transport is external to this Java module; this default keeps DI registration usable
 * for contract tests without pretending Java owns the live Roblox HTTP bridge.
 */
public final class RobloxNoopControlCommandClient implements IRobloxControlCommandClient, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return FrontendCommandVerificationResult.rejected(envelope, "Roblox control transport is provided by the Luau runtime.");
    }
}
