package org.tavall.minecraft.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.Map;
import java.util.Set;

public interface IMinecraftProxyConfig extends IDependencyInjectableInterface {
    String commandPermission();

    String adminPermission();

    String serverId();

    Map<String, String> instanceServerMappings();

    Set<String> ownerUsernames();

    Set<String> adminUsernames();

    boolean allowConsoleAdmin();
}
