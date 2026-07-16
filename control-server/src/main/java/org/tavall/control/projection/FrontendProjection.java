package org.tavall.control.projection;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.common.GamePlatform;
import org.tavall.control.common.MetadataMaps;

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
