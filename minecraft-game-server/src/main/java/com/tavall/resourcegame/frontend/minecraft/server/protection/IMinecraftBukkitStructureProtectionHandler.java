package org.tavall.minecraft.server.protection;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.Optional;
import java.util.UUID;

public interface IMinecraftBukkitStructureProtectionHandler extends Listener, IDependencyInjectableInterface {
    void registerCastle(UUID ownerId, Location anchor);

    void registerBuilding(UUID ownerId, Location anchor, String buildingType);

    void clearCastle(UUID ownerId);

    void clearBuilding(UUID ownerId);

    boolean isProtected(Location location);

    Optional<String> protectionReason(Location location);
}
