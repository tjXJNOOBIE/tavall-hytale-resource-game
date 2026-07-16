package org.tavall.control.cloud;

import java.time.Instant;
import java.util.UUID;

public final class NodeHeartbeatHandler implements INodeHeartbeatHandler, CloudControlDomain {
    public boolean heartbeat(UUID nodeId, Instant now) {
        return getCloudRepository().findNode(nodeId)
                .map(node -> {
                    getCloudRepository().saveNode(node.withStatus(CloudNodeStatus.ONLINE, now));
                    return true;
                })
                .orElse(false);
    }
}
