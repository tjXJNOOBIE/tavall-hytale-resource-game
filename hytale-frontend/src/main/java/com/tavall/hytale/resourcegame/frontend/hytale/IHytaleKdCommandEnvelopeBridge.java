package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.Map;

public interface IHytaleKdCommandEnvelopeBridge extends IDependencyInjectableInterface {
    FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    );
}
