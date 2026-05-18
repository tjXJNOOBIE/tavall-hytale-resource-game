package org.tavall.control.runtime;

import org.tavall.control.common.MetadataMaps;

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
