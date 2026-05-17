package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record FrontendProjection(
        UUID projectionId,
        GamePlatform platform,
        String canonicalObjectId,
        ProjectionObjectType objectType,
        GlobalAssetId globalAssetId,
        Optional<String> platformAssetReference,
        String displayName,
        Optional<CanonicalLocation> location,
        String state,
        List<InteractionAction> interactionActions,
        Map<String, String> metadata
) {
    public FrontendProjection {
        Objects.requireNonNull(projectionId, "projectionId");
        Objects.requireNonNull(platform, "platform");
        if (canonicalObjectId == null || canonicalObjectId.isBlank()) {
            throw new IllegalArgumentException("canonicalObjectId is required.");
        }
        Objects.requireNonNull(objectType, "objectType");
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        platformAssetReference = platformAssetReference == null ? Optional.empty() : platformAssetReference;
        displayName = displayName == null ? "" : displayName;
        location = location == null ? Optional.empty() : location;
        state = state == null ? "" : state;
        interactionActions = interactionActions == null ? List.of() : List.copyOf(interactionActions);
        metadata = MetadataMaps.immutable(metadata);
    }
}
