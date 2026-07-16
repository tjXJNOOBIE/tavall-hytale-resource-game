package org.tavall.control.cloud;

import java.util.Map;

public record VolumeRequest(
        String mountPath,
        int sizeGb,
        StorageType storageType,
        boolean persistent,
        Map<String, String> metadata
) {
    public VolumeRequest {
        storageType = storageType == null ? StorageType.LOCAL_BULK : storageType;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
