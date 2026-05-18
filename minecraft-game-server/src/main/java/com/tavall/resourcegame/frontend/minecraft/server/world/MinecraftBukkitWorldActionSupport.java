package org.tavall.minecraft.server.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class MinecraftBukkitWorldActionSupport {
    private MinecraftBukkitWorldActionSupport() {
    }

    public static String token(String[] tokens, int index) {
        if (tokens == null || index < 0 || index >= tokens.length) {
            return null;
        }
        return tokens[index];
    }

    public static String joined(String[] tokens, int startIndex) {
        if (tokens == null || startIndex >= tokens.length) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = startIndex; index < tokens.length; index++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(tokens[index]);
        }
        return builder.toString().trim();
    }

    public static Location placeAnchor(Player player, Location previousAnchor, double offsetDistance, double verticalOffset) {
        World world = player.getWorld();
        Location location = player.getLocation().clone();
        Vector direction = location.getDirection();
        if (direction.lengthSquared() < 0.001D) {
            direction = new Vector(1.0D, 0.0D, 0.0D);
        } else {
            direction.normalize();
        }
        location.add(direction.multiply(offsetDistance));
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int highest = world.getHighestBlockYAt(x, z);
        int y = Math.max(highest + 2, player.getLocation().getBlockY() + (int) Math.round(verticalOffset));
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        return location;
    }

    public static void clearTrackedEntities(World world, Location center, double radius, String tag) {
        Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);
        for (Entity entity : nearby) {
            if (entity == null || !entity.getScoreboardTags().contains(tag)) {
                continue;
            }
            entity.remove();
        }
    }

    public static int countTaggedEntities(World world, Location center, double radius, String tag) {
        int count = 0;
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity != null && entity.getScoreboardTags().contains(tag)) {
                count += 1;
            }
        }
        return count;
    }

    public static void clearCube(World world, Location center, int halfX, int halfY, int halfZ, String tag) {
        if (center == null) {
            return;
        }
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        for (int x = centerX - halfX; x <= centerX + halfX; x++) {
            for (int y = centerY - halfY; y <= centerY + halfY; y++) {
                for (int z = centerZ - halfZ; z <= centerZ + halfZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
        clearTrackedEntities(world, center, Math.max(halfX, Math.max(halfY, halfZ)) + 4.0D, tag);
        clearTrackedEntities(world, center, Math.max(halfX, Math.max(halfY, halfZ)) + 4.0D, "tavall.kingdom.hologram");
    }

    public static void fillPad(World world, int centerX, int centerY, int centerZ, int radiusX, int radiusZ, Material material) {
        for (int x = centerX - radiusX; x <= centerX + radiusX; x++) {
            for (int z = centerZ - radiusZ; z <= centerZ + radiusZ; z++) {
                world.getBlockAt(x, centerY, z).setType(material, false);
            }
        }
    }

    public static void setColumn(World world, int x, int y, int z, int height, Material material) {
        for (int offset = 0; offset < height; offset++) {
            world.getBlockAt(x, y + offset, z).setType(material, false);
        }
    }

    public static void setBlock(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
    }

    public static void spawnHologramStack(World world, Location location, List<String> lines) {
        Location cursor = location.clone();
        for (String line : lines) {
            ArmorStand stand = (ArmorStand) world.spawnEntity(cursor, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setCustomName(line);
            stand.setCustomNameVisible(true);
            stand.addScoreboardTag("tavall.kingdom.helper");
            stand.addScoreboardTag("tavall.kingdom.hologram");
            cursor = cursor.clone().add(0.0D, 0.25D, 0.0D);
            stand.setCollidable(false);
        }
    }

    public static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Kingdom";
        }
        String normalized = value.replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    public static String normalizeType(String token, String defaultValue) {
        if (token == null || token.isBlank()) {
            return defaultValue;
        }
        return token.toLowerCase(Locale.ROOT);
    }
}
