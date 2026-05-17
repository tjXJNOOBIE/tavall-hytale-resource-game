package com.tavall.resourcegame.middleware.control;

import java.util.Map;

public interface ControlSurfaceLaunchHandler {
    ControlSurfaceLaunchResult startSurface(String surfaceName, Map<String, String> arguments);
}
