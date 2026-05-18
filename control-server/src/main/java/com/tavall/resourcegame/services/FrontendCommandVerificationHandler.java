package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.bootstrap.IResourceGameDomain;
import org.tavall.control.runtime.IFrontendCommandVerificationService;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendKdCommandInputFormatter;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FrontendCommandVerificationHandler implements IFrontendCommandVerificationService, IResourceGameDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult verifyHytaleKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            Map<String, String> sourceMetadata
    ) {
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.HYTALE,
                platformAccountId,
                platformDisplayName,
                new FrontendKdCommandInputFormatter().rawKdInput(commandTokens),
                "hytale-kd-" + UUID.randomUUID(),
                mergedSourceMetadata(sourceMetadata)
        );
        return getFrontendControlCommandClient().submitCommand(envelope);
    }

    /**
     * Hytale is a single-surface runtime, so these markers prevent the control plane from
     * interpreting commands as proxy/global Minecraft input.
     */
    private Map<String, String> mergedSourceMetadata(Map<String, String> sourceMetadata) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("server", getFrontendControlConfig().serverId());
        metadata.put("surfaceIdentity", "HYTALE_SINGLE_SERVER");
        metadata.put("serverDataSource", "hytale-single-server");
        metadata.putAll(sourceMetadata == null ? Map.of() : sourceMetadata);
        return Map.copyOf(metadata);
    }
}


