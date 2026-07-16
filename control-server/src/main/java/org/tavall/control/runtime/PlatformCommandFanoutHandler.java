package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.common.GamePlatform;

import java.util.List;
import java.util.Map;

public final class PlatformCommandFanoutHandler implements ControlCommandDomain, IDependencyInjectableConcrete {
    public List<PlatformCommandResult> fanoutCommand(ControlCommand command, List<String> changedObjectIds) {
        List<PlatformCommandResult> platformResults = getPlatformFanoutTargetResolver()
                .resolveTargets(command, getPlatformFrontendAdapterRegistry().adaptersByPlatform())
                .stream()
                .map(adapter -> fanoutToAdapter(adapter, command, changedObjectIds))
                .toList();
        getOptionalControlPlatformFanoutRetryHandler()
                .ifPresent(handler -> handler.recordFailedFanout(command, changedObjectIds, platformResults, java.time.Instant.now()));
        return platformResults;
    }

    public List<PlatformConnectionStatus> platformStatuses() {
        return getPlatformFrontendAdapterRegistry().adaptersByPlatform().values().stream()
                .map(PlatformFrontendAdapter::getConnectionStatus)
                .toList();
    }

    public Map<GamePlatform, PlatformFrontendAdapter> adaptersByPlatform() {
        return getPlatformFrontendAdapterRegistry().adaptersByPlatform();
    }

    private PlatformCommandResult fanoutToAdapter(PlatformFrontendAdapter adapter, ControlCommand command, List<String> changedObjectIds) {
        return switch (command.commandType()) {
            case BROADCAST_PLATFORM_MESSAGE -> adapter.broadcastMessage(command, command.argument("message"));
            case SYNC_PLATFORM_STATE -> adapter.syncObject(command, changedObjectIds);
            case REFRESH_FRONTEND_PROJECTIONS -> adapter.refreshProjection(command, changedObjectIds);
            default -> {
                PlatformCommandResult eventResult = adapter.sendControlEvent(command, changedObjectIds);
                if (!eventResult.success()) {
                    yield eventResult;
                }
                yield adapter.refreshProjection(command, changedObjectIds);
            }
        };
    }
}
