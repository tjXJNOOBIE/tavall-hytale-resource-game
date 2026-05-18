package org.tavall.control.runtime;

import java.util.Map;

public interface ControlSurfaceLaunchHandler {
    ControlSurfaceLaunchResult startSurface(String surfaceName, Map<String, String> arguments);
}
