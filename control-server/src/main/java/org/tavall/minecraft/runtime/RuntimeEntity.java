package org.tavall.minecraft.runtime;

import org.tavall.minecraft.framework.game.AssetId;

import java.util.UUID;

public record RuntimeEntity(UUID entityId, AssetId assetId, WorldPosition position, String displayLabel) {
}
