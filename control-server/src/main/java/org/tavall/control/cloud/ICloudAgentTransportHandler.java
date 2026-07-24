package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.io.IOException;
import java.util.List;

public interface ICloudAgentTransportHandler extends IDependencyInjectableInterface {
    boolean sendHeartbeat(CloudAgentHeartbeatPayload heartbeat) throws IOException, InterruptedException;

    List<CloudCommand> pollCommands() throws IOException, InterruptedException;

    boolean reportResult(CloudCommandResult result) throws IOException, InterruptedException;
}
