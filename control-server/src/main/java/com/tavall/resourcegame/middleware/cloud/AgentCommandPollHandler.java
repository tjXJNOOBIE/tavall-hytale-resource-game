package org.tavall.control.cloud;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AgentCommandPollHandler implements IAgentCommandPollHandler, ICloudControlDomain {
    /**
     * Polling marks commands as sent so repeated agent polls do not duplicate work before a result arrives.
     */
    public List<CloudCommand> poll(UUID nodeId, int maxCommands, Instant now) {
        List<CloudCommand> availableCommands = new ArrayList<>();
        for (CloudCommand command : getCloudRepository().findPendingCommands(nodeId)) {
            if (isExpired(command, now)) {
                getCloudRepository().saveCommand(command.withStatus(CloudCommandStatus.EXPIRED,
                        Optional.of("Command expired before agent poll.")));
            } else {
                availableCommands.add(command);
            }
        }
        return availableCommands.stream()
                .limit(Math.max(0, maxCommands))
                .map(command -> {
                    CloudCommand sent = command.withStatus(CloudCommandStatus.SENT, Optional.empty());
                    getCloudRepository().saveCommand(sent);
                    return sent;
                })
                .toList();
    }

    private boolean isExpired(CloudCommand command, Instant now) {
        return command.expiresAt().isPresent() && !now.isBefore(command.expiresAt().get());
    }
}
