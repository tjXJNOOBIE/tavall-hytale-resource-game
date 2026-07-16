package org.tavall.control.trade;

import java.util.Objects;
import java.util.UUID;

public record TradeRouteId(UUID value) {
    public TradeRouteId {
        Objects.requireNonNull(value, "value");
    }

    public static TradeRouteId random() {
        return new TradeRouteId(UUID.randomUUID());
    }
}
