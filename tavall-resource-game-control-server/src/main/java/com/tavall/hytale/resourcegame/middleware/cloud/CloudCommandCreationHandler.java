package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CloudCommandCreationHandler implements ICloudCommandCreationHandler, ICloudControlDomain {
    public CloudCommand create(UUID nodeId, CloudCommandType commandType, String payloadJson, UUID requestedBy, UUID correlationId, Instant now) {
        CloudCommand command = new CloudCommand(UUID.randomUUID(), nodeId, commandType, payloadJson, requestedBy, now,
                CloudCommandStatus.CREATED, Optional.empty(), correlationId, Optional.empty(), Optional.empty(), Map.of());
        getCloudRepository().saveCommand(command);
        return command;
    }
}
