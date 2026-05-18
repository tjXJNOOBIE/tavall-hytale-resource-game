package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.bootstrap.IResourceGameDomain;
import org.tavall.control.runtime.IFrontendCommandVerificationHandler;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendKdCommandInputFormatter;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FrontendCommandVerificationHandler implements IFrontendCommandVerificationHandler, IResourceGameDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult verifyMinecraftKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            Map<String, String> sourceMetadata
    ) {
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                platformAccountId,
                platformDisplayName,
                new FrontendKdCommandInputFormatter().rawKdInput(commandTokens),
                "minecraft-kd-" + UUID.randomUUID(),
                mergedSourceMetadata(sourceMetadata)
        );
        return getFrontendControlCommandClient().submitCommand(envelope);
    }

    /**
     * Minecraft is a single-surface runtime, so these markers prevent the control plane from
     * interpreting commands as proxy/global Minecraft input.
     */
    private Map<String, String> mergedSourceMetadata(Map<String, String> sourceMetadata) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("server", getFrontendControlConfig().serverId());
        metadata.put("surfaceIdentity", "MINECRAFT_SINGLE_SERVER");
        metadata.put("serverDataSource", "minecraft-single-server");
        metadata.putAll(sourceMetadata == null ? Map.of() : sourceMetadata);
        return Map.copyOf(metadata);
    }
}


