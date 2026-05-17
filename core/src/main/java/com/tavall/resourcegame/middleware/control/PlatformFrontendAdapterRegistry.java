package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlatformFrontendAdapterRegistry implements IDependencyInjectableConcrete {
    private final EnumMap<GamePlatform, PlatformFrontendAdapter> adaptersByPlatform = new EnumMap<>(GamePlatform.class);

    public void registerAdapters(List<PlatformFrontendAdapter> platformAdapters) {
        adaptersByPlatform.clear();
        for (PlatformFrontendAdapter adapter : platformAdapters) {
            adaptersByPlatform.put(adapter.getPlatform(), adapter);
        }
    }

    public Map<GamePlatform, PlatformFrontendAdapter> adaptersByPlatform() {
        return Map.copyOf(adaptersByPlatform);
    }
}
