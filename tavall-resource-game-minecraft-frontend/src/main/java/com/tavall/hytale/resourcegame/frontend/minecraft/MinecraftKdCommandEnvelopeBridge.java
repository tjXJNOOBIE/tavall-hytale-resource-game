package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendKdCommandInputFormatter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MinecraftKdCommandEnvelopeBridge {
    private final MinecraftFrontendCommandEnvelopeFactory envelopeFactory;
    private final FrontendKdCommandInputFormatter commandInputFormatter;

    public MinecraftKdCommandEnvelopeBridge() {
        this(new MinecraftFrontendCommandEnvelopeFactory(), new FrontendKdCommandInputFormatter());
    }

    public MinecraftKdCommandEnvelopeBridge(
            MinecraftFrontendCommandEnvelopeFactory envelopeFactory,
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
        return envelopeFactory.commandEnvelope(
                platformAccountId,
                platformDisplayName,
                commandInputFormatter.rawKdInput(commandTokens),
                correlationId,
                sourceMetadata
        );
    }
}
