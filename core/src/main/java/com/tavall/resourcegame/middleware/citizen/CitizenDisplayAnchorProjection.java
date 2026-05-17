package com.tavall.resourcegame.middleware.citizen;

import java.util.Map;

public record CitizenDisplayAnchorProjection(
        String anchorId,
        CitizenSummaryScope scope,
        CitizenAnchorType anchorType,
        String displayText,
        int count,
        String globalAssetId,
        Map<String, String> metadata
) {
}
