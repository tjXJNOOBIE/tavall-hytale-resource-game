package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HytaleKdCommandEnvelopeBridge {
    private final HytaleFrontendCommandEnvelopeFactory envelopeFactory;

    public HytaleKdCommandEnvelopeBridge() {
        this(new HytaleFrontendCommandEnvelopeFactory());
    }

    public HytaleKdCommandEnvelopeBridge(HytaleFrontendCommandEnvelopeFactory envelopeFactory) {
        this.envelopeFactory = Objects.requireNonNull(envelopeFactory, "envelopeFactory");
    }

    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        String rawInput = rawKdInput(commandTokens);
        return envelopeFactory.commandEnvelope(platformAccountId, platformDisplayName, rawInput, correlationId, sourceMetadata);
    }

    public String rawKdInput(List<String> commandTokens) {
        if (commandTokens == null || commandTokens.isEmpty()) {
            return "/kd";
        }
        String firstToken = commandTokens.getFirst();
        if ("kd".equalsIgnoreCase(firstToken) || "kingdom".equalsIgnoreCase(firstToken)) {
            return "/" + String.join(" ", commandTokens);
        }
        return "/kd " + String.join(" ", commandTokens);
    }
}
