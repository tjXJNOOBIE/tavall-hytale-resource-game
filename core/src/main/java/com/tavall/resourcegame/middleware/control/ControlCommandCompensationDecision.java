package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.Map;
import java.util.Objects;

public record ControlCommandCompensationDecision(
        ControlCommandId commandId,
        boolean compensationRequired,
        boolean manualReviewRequired,
        String reason,
        Map<String, String> metadata
) {
    public ControlCommandCompensationDecision {
        Objects.requireNonNull(commandId, "commandId");
        reason = reason == null ? "" : reason;
        metadata = MetadataMaps.immutable(metadata);
    }
}
