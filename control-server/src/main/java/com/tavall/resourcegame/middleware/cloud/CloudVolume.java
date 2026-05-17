package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.UUID;

public record CloudVolume(
        UUID volumeId,
        UUID workloadId,
        UUID nodeId,
        String mountPath,
        int sizeGb,
        StorageType storageType,
        boolean persistent,
        VolumeState desiredState,
        VolumeState actualState,
        Map<String, String> metadata
) {
    public CloudVolume {
        storageType = storageType == null ? StorageType.LOCAL_BULK : storageType;
        desiredState = desiredState == null ? VolumeState.REQUESTED : desiredState;
        actualState = actualState == null ? VolumeState.REQUESTED : actualState;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
