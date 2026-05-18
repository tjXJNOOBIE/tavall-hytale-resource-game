package org.tavall.control.cloud;

import java.util.Map;
import java.util.UUID;

public final class VolumeProvisioningHandler implements IVolumeProvisioningHandler, CloudControlDomain {
    @Override
    public CloudVolume createRequestedVolume(UUID workloadId, UUID nodeId, VolumeRequest request) {
        if (request.mountPath() == null || request.mountPath().isBlank()) {
            throw new IllegalArgumentException("Volume mount path is required.");
        }
        if (request.sizeGb() <= 0) {
            throw new IllegalArgumentException("Volume size must be greater than zero.");
        }
        if (request.persistent() && request.storageType() == StorageType.TEMPORARY) {
            throw new IllegalArgumentException("Persistent workloads cannot use temporary storage.");
        }
        CloudVolume volume = new CloudVolume(
                UUID.randomUUID(),
                workloadId,
                nodeId,
                request.mountPath(),
                request.sizeGb(),
                request.storageType(),
                request.persistent(),
                VolumeState.REQUESTED,
                VolumeState.REQUESTED,
                Map.copyOf(request.metadata())
        );
        getCloudRepository().saveVolume(volume);
        return volume;
    }
}
