package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CloudCommandCreationHandler {
    private final InMemoryCloudRepository repository;

    public CloudCommandCreationHandler(InMemoryCloudRepository repository) {
        this.repository = repository;
    }

    public CloudCommand create(UUID nodeId, CloudCommandType commandType, String payloadJson, UUID requestedBy, UUID correlationId, Instant now) {
        CloudCommand command = new CloudCommand(UUID.randomUUID(), nodeId, commandType, payloadJson, requestedBy, now,
                CloudCommandStatus.CREATED, Optional.empty(), correlationId, Optional.empty(), Optional.empty(), Map.of());
        repository.saveCommand(command);
        return command;
    }
}
