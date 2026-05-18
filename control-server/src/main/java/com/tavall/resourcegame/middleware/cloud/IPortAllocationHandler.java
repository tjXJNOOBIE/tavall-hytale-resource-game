package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface IPortAllocationHandler extends IDependencyInjectableInterface {
    PortAllocation allocate(UUID workloadId, UUID nodeId, PortProtocol protocol, int publicPort, int internalPort, Instant now);
}
