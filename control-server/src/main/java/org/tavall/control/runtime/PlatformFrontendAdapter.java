package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;

import java.util.List;

public interface PlatformFrontendAdapter {
    GamePlatform getPlatform();

    PlatformConnectionStatus getConnectionStatus();

    boolean supportsCommandFanout(ControlCommand command);

    PlatformCommandResult refreshProjection(ControlCommand command, List<String> changedObjectIds);

    PlatformCommandResult sendControlEvent(ControlCommand command, List<String> changedObjectIds);

    PlatformCommandResult broadcastMessage(ControlCommand command, String message);

    PlatformCommandResult syncObject(ControlCommand command, List<String> changedObjectIds);
}
