package com.tavall.resourcegame.middleware.common;

import java.util.Map;

public final class MetadataMaps {
    private MetadataMaps() {
    }

    public static Map<String, String> immutable(Map<String, String> metadata) {
        return metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
