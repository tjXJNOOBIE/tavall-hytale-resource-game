package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitServerConfig extends IDependencyInjectableInterface {
    String serverId();

    String proxyId();

    long snapshotIntervalTicks();

    String resourcePackPath();

    String resourcePackUrl();

    String resourcePackPrompt();

    boolean resourcePackForce();

    int resourcePackFormat();
}
