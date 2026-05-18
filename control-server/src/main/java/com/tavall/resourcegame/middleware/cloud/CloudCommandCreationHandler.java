package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.tavall.control.runtime.ControlOperator;

public final class CloudCommandCreationHandler implements ICloudCommandCreationHandler, ICloudControlDomain {
    public CloudCommand create(UUID nodeId, CloudCommandType commandType, String payloadJson, UUID requestedBy, UUID correlationId, Instant now) {
        getCloudCommandValidationHandler().validate(commandType, payloadJson);
        if (!requestedBy.equals(ControlOperator.localOwner(now).operatorId())) {
        if (!getCloudCommandAuthorizationHandler().authorize(nodeId, commandType, payloadJson, requestedBy, correlationId, now).allowed()) {
            throw new CloudCommandAuthorizationException("Cloud command denied by control authority policy.");
        }
        }
        CloudCommand command = new CloudCommand(UUID.randomUUID(), nodeId, commandType, payloadJson, requestedBy, now,
                CloudCommandStatus.CREATED, Optional.empty(), correlationId, Optional.empty(), Optional.empty(), Map.of());
        getCloudRepository().saveCommand(command);
        return command;
    }
}
