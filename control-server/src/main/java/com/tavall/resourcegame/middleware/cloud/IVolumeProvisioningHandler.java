package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IVolumeProvisioningHandler extends IDependencyInjectableInterface {
    CloudVolume createRequestedVolume(UUID workloadId, UUID nodeId, VolumeRequest request);
}
