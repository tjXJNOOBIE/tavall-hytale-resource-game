package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CloudAgentRuntime implements ICloudAgentRuntime, CloudAgentDomain, IDependencyInjectableConcrete {
    @Override
    public CloudAgentRuntimeCycleResult runOnce(Instant now) throws IOException, InterruptedException {
        boolean heartbeatAccepted = getCloudAgentTransportHandler().sendHeartbeat(new CloudAgentHeartbeatPayload(
                getCloudAgentRuntimeConfig().nodeId(),
                getCloudAgentRuntimeConfig().agentId(),
                getCloudAgentRuntimeConfig().agentVersion(),
                now,
                Map.of("runtime", "plain-java-agent")
        ));
        List<CloudCommand> commands = getCloudAgentTransportHandler().pollCommands();
        int reported = 0;
        for (CloudCommand command : commands) {
            CloudCommandResult result = getAgentCommandExecutionHandler().execute(command, now);
            if (getCloudAgentTransportHandler().reportResult(result)) {
                reported++;
            }
        }
        return new CloudAgentRuntimeCycleResult(heartbeatAccepted, commands.size(), commands.size(), reported);
    }

    @Override
    public void runForever() throws IOException, InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            runOnce(Instant.now());
            Thread.sleep(getCloudAgentRuntimeConfig().pollIntervalMillis());
        }
    }
}
