package org.tavall.minecraft.server.snapshot;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotSubmitHandler extends IDependencyInjectableInterface {
    void submitSnapshotQuietly();
}
