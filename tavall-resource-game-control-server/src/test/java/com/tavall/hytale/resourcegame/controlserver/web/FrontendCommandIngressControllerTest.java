package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FrontendCommandIngressControllerTest {
    @Test
    void discordHttpIngressDispatchesThroughCanonicalFrontendHandler() {
        FrontendCommandIngressController controller = new FrontendCommandIngressController(
                ControlCommandRuntimeFactory.createInMemoryRuntime()
        );
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.DISCORD,
                "discord-user",
                "Citizen",
                "/kingdom ui discord-live-verify",
                "corr-discord-http",
                Map.of("guildId", "project-novus")
        );

        FrontendCommandVerificationResult result = controller.ingestFrontendCommand(envelope);

        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
        assertTrue(result.success());
        assertEquals("ui", result.metadata().get("verifiedCategory"));
    }
}
