package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;

import java.util.List;
import java.util.Map;

public final class PlatformFanoutTargetResolver {
    public List<PlatformFrontendAdapter> resolveTargets(ControlCommand command, Map<GamePlatform, PlatformFrontendAdapter> adaptersByPlatform) {
        if (command.targetPlatforms().isEmpty()) {
            return adaptersByPlatform.values().stream()
                    .filter(adapter -> adapter.supportsCommandFanout(command))
                    .toList();
        }
        return command.targetPlatforms().stream()
                .map(adaptersByPlatform::get)
                .filter(adapter -> adapter != null && adapter.supportsCommandFanout(command))
                .toList();
    }
}
