package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class RecordingHytaleControlCommandClient implements IHytaleControlCommandClient, IDependencyInjectableConcrete {
    private final AtomicReference<FrontendCommandEnvelope> submittedEnvelope;

    RecordingHytaleControlCommandClient(AtomicReference<FrontendCommandEnvelope> submittedEnvelope) {
        this.submittedEnvelope = submittedEnvelope;
    }

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        submittedEnvelope.set(envelope);
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED,
                true,
                "verified",
                "cmd-hytale",
                "COMPLETED",
                Map.of("verifiedCategory", "ui")
        );
    }
}
