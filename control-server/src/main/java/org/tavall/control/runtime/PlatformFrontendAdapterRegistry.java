package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.common.GamePlatform;

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
