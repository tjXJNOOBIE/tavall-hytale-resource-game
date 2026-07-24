package org.tavall.minecraft.permissions;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.minecraft.bridge.IMinecraftFrontendBridgeDependencyAccess;

public interface IMinecraftFrontendPermissionDependencyAccess extends IMinecraftFrontendBridgeDependencyAccess {
    default IMinecraftVelocityPermissionResolver getMinecraftVelocityPermissionResolver() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityPermissionResolver.class);
    }

    default IMinecraftVelocityCommandPermissionHandler getMinecraftVelocityCommandPermissionHandler() {
        return DependencyLoaderAccess.requireInstance(IMinecraftVelocityCommandPermissionHandler.class);
    }
}
