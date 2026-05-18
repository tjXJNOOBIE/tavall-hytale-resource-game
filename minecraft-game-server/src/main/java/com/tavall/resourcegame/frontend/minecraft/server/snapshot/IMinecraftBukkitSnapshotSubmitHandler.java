package org.tavall.minecraft.server.snapshot;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotSubmitHandler extends IDependencyInjectableInterface {
    void submitSnapshotQuietly();
}
