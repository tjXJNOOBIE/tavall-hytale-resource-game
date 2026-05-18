package org.tavall.control.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class RecordingCloudAgentTransportHandler implements ICloudAgentTransportHandler {
    private final List<CloudCommand> commands = new ArrayList<CloudCommand>();
    private final List<CloudCommandResult> results = new ArrayList<CloudCommandResult>();
    private int heartbeatCount;

    public void addCommand(CloudCommand command) {
        commands.add(command);
    }

    public int heartbeatCount() {
        return heartbeatCount;
    }

    public List<CloudCommandResult> results() {
        return List.copyOf(results);
    }

    @Override
    public boolean sendHeartbeat(CloudAgentHeartbeatPayload heartbeat) {
        heartbeatCount++;
        return true;
    }

    @Override
    public List<CloudCommand> pollCommands() throws IOException, InterruptedException {
        return List.copyOf(commands);
    }

    @Override
    public boolean reportResult(CloudCommandResult result) {
        results.add(result);
        return true;
    }
}
