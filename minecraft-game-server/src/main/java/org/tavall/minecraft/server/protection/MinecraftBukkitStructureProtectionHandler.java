package org.tavall.minecraft.server.protection;

import org.tavall.minecraft.server.MinecraftBukkitServerDomain;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MinecraftBukkitStructureProtectionHandler implements IMinecraftBukkitStructureProtectionHandler, MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final String CASTLE_REASON = "Castle structures are protected. Use the castle menu instead.";
    private static final String BUILDING_REASON = "Building structures are protected. Use the building menu instead.";

    private final ConcurrentMap<UUID, ProtectedStructureRegion> castleRegions = new ConcurrentHashMap<UUID, ProtectedStructureRegion>();
    private final ConcurrentMap<UUID, ProtectedStructureRegion> buildingRegions = new ConcurrentHashMap<UUID, ProtectedStructureRegion>();

    @Override
    public void registerCastle(UUID ownerId, Location anchor) {
        register(castleRegions, ownerId, anchor, 6, 5, 6, "castle", CASTLE_REASON);
    }

    @Override
    public void registerBuilding(UUID ownerId, Location anchor, String buildingType) {
        String label = buildingType == null || buildingType.isBlank() ? "building" : buildingType;
        register(buildingRegions, ownerId, anchor, 5, 5, 5, label, BUILDING_REASON);
    }

    @Override
    public void clearCastle(UUID ownerId) {
        if (ownerId != null) {
            castleRegions.remove(ownerId);
        }
    }

    @Override
    public void clearBuilding(UUID ownerId) {
        if (ownerId != null) {
            buildingRegions.remove(ownerId);
        }
    }

    @Override
    public boolean isProtected(Location location) {
        return protectionReason(location).isPresent();
    }

    @Override
    public Optional<String> protectionReason(Location location) {
        if (location == null) {
            return Optional.empty();
        }
        for (ProtectedStructureRegion region : castleRegions.values()) {
            if (region.contains(location)) {
                return Optional.of(region.reason());
            }
        }
        for (ProtectedStructureRegion region : buildingRegions.values()) {
            if (region.contains(location)) {
                return Optional.of(region.reason());
            }
        }
        return Optional.empty();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event == null || event.getBlock() == null) {
            return;
        }
        protectionReason(event.getBlock().getLocation()).ifPresent(reason -> {
            event.setCancelled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().sendMessage(reason);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null || event.getBlockPlaced() == null) {
            return;
        }
        protectionReason(event.getBlockPlaced().getLocation()).ifPresent(reason -> {
            event.setCancelled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().sendMessage(reason);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event == null) {
            return;
        }
        event.blockList().removeIf(block -> block != null && isProtected(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event == null) {
            return;
        }
        event.blockList().removeIf(block -> block != null && isProtected(block.getLocation()));
    }

    private void register(
            ConcurrentMap<UUID, ProtectedStructureRegion> regions,
            UUID ownerId,
            Location anchor,
            int halfX,
            int halfY,
            int halfZ,
            String label,
            String reason
    ) {
        if (ownerId == null || anchor == null) {
            return;
        }
        regions.put(ownerId, ProtectedStructureRegion.from(anchor, halfX, halfY, halfZ, label, reason));
    }

    private record ProtectedStructureRegion(
            String worldName,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ,
            String label,
            String reason
    ) {
        static ProtectedStructureRegion from(Location anchor, int halfX, int halfY, int halfZ, String label, String reason) {
            String worldName = anchor.getWorld() == null ? "" : anchor.getWorld().getName();
            int centerX = anchor.getBlockX();
            int centerY = anchor.getBlockY();
            int centerZ = anchor.getBlockZ();
            return new ProtectedStructureRegion(
                    worldName,
                    centerX - halfX,
                    centerX + halfX,
                    centerY - halfY,
                    centerY + halfY,
                    centerZ - halfZ,
                    centerZ + halfZ,
                    label == null ? "structure" : label.toLowerCase(Locale.ROOT),
                    reason
            );
        }

        boolean contains(Location location) {
            if (location == null || location.getWorld() == null) {
                return false;
            }
            if (!worldName.equals(location.getWorld().getName())) {
                return false;
            }
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }
    }
}
