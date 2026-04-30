package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlatformCommandFanoutHandler {
    private final Map<GamePlatform, PlatformFrontendAdapter> adaptersByPlatform;
    private final PlatformFanoutTargetResolver targetResolver;
    private final java.util.Optional<ControlPlatformFanoutRetryHandler> retryHandler;

    public PlatformCommandFanoutHandler(List<PlatformFrontendAdapter> platformAdapters, PlatformFanoutTargetResolver targetResolver) {
        this(platformAdapters, targetResolver, null);
    }

    public PlatformCommandFanoutHandler(List<PlatformFrontendAdapter> platformAdapters, PlatformFanoutTargetResolver targetResolver, ControlPlatformFanoutRetryHandler retryHandler) {
        EnumMap<GamePlatform, PlatformFrontendAdapter> adapters = new EnumMap<>(GamePlatform.class);
        for (PlatformFrontendAdapter adapter : platformAdapters) {
            adapters.put(adapter.getPlatform(), adapter);
        }
        this.adaptersByPlatform = Map.copyOf(adapters);
        this.targetResolver = targetResolver;
        this.retryHandler = java.util.Optional.ofNullable(retryHandler);
    }

    public List<PlatformCommandResult> fanoutCommand(ControlCommand command, List<String> changedObjectIds) {
        List<PlatformCommandResult> platformResults = targetResolver.resolveTargets(command, adaptersByPlatform).stream()
                .map(adapter -> fanoutToAdapter(adapter, command, changedObjectIds))
                .toList();
        retryHandler.ifPresent(handler -> handler.recordFailedFanout(command, changedObjectIds, platformResults, java.time.Instant.now()));
        return platformResults;
    }

    public List<PlatformConnectionStatus> platformStatuses() {
        return adaptersByPlatform.values().stream()
                .map(PlatformFrontendAdapter::getConnectionStatus)
                .toList();
    }

    public Map<GamePlatform, PlatformFrontendAdapter> adaptersByPlatform() {
        return adaptersByPlatform;
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
