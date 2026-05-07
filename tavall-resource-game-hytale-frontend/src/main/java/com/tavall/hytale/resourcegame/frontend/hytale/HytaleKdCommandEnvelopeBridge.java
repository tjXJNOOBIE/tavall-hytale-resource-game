package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendKdCommandInputFormatter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HytaleKdCommandEnvelopeBridge {
    private final HytaleFrontendCommandEnvelopeFactory envelopeFactory;
    private final FrontendKdCommandInputFormatter commandInputFormatter;

    public HytaleKdCommandEnvelopeBridge() {
        this(new HytaleFrontendCommandEnvelopeFactory(), new FrontendKdCommandInputFormatter());
    }

    public HytaleKdCommandEnvelopeBridge(
            HytaleFrontendCommandEnvelopeFactory envelopeFactory,
            FrontendKdCommandInputFormatter commandInputFormatter
    ) {
        this.envelopeFactory = Objects.requireNonNull(envelopeFactory, "envelopeFactory");
        this.commandInputFormatter = Objects.requireNonNull(commandInputFormatter, "commandInputFormatter");
    }

    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        String rawInput = commandInputFormatter.rawKdInput(commandTokens);
        return envelopeFactory.commandEnvelope(platformAccountId, platformDisplayName, rawInput, correlationId, sourceMetadata);
    }
}
