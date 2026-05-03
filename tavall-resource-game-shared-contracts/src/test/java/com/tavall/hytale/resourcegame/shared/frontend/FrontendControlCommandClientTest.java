package com.tavall.hytale.resourcegame.shared.frontend;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class FrontendControlCommandClientTest {
    @Test
    void commandClientSubmitsSharedEnvelopeToControlPlaneBoundary() {
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        FrontendControlCommandClient client = envelope -> {
            submittedEnvelope.set(envelope);
            return new FrontendCommandVerificationResult(
                    envelope,
                    FrontendCommandVerificationState.DISPATCHED,
                    true,
                    "dispatched",
                    "cmd-shared",
                    "COMPLETED",
                    Map.of("controlConsoleInput", "tick healing 1")
            );
        };
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                "minecraft-player",
                "Miner",
                "/kd tick run 1",
                "corr-shared",
                Map.of("server", "kingdoms")
        );

        FrontendCommandVerificationResult result = client.submitCommand(envelope);

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("/kd tick run 1", submittedEnvelope.get().rawInput());
    }
}
