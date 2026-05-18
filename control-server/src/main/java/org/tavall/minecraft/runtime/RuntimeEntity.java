package org.tavall.minecraft.runtime;

import java.util.UUID;

public record RuntimeEntity(UUID entityId, AssetId assetId, WorldPosition position, String displayLabel) {
}
