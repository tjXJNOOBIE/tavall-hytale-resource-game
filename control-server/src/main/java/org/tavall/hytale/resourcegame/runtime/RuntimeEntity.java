package org.tavall.minecraft.runtime;

import java.util.UUID;

public record RuntimeEntity(UUID entityId, HytaleAssetId assetId, WorldPosition position, String displayLabel) {
}
