package org.tavall.minecraft.server;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.minecraft.server.world.MinecraftBukkitWorldActionSupport;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MinecraftBukkitStructureWorldActionHandler implements IMinecraftBukkitStructureWorldActionHandler, MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final String CASTLE_TAG = "tavall.kingdom.castle";
    private static final String BUILDING_TAG = "tavall.kingdom.building";
    private static final String INTERIOR_TAG = "tavall.kingdom.interior";
    private static final String NODE_TAG = "tavall.kingdom.node";

    private final ConcurrentMap<UUID, Location> castleAnchors = new ConcurrentHashMap<UUID, Location>();
    private final ConcurrentMap<UUID, Location> buildingAnchors = new ConcurrentHashMap<UUID, Location>();
    private final ConcurrentMap<UUID, Location> interiorAnchors = new ConcurrentHashMap<UUID, Location>();
    private final ConcurrentMap<UUID, Location> nodeAnchors = new ConcurrentHashMap<UUID, Location>();

    @Override
    public Optional<String> apply(Player player, String[] tokens, FrontendCommandVerificationResult result) {
        if (player == null || tokens == null || tokens.length < 2 || result == null || !result.success()) {
            return Optional.empty();
        }
        String root = MinecraftBukkitWorldActionSupport.token(tokens, 1);
        if (root == null) {
            return Optional.empty();
        }
        if ("place".equalsIgnoreCase(root)) {
            return applyPlacement(player, tokens);
        }
        if ("buildings".equalsIgnoreCase(root) || "building".equalsIgnoreCase(root)) {
            return applyBuildings(player, tokens);
        }
        if ("castle".equalsIgnoreCase(root)) {
            return applyCastle(player, tokens);
        }
        if ("interior".equalsIgnoreCase(root)) {
            return applyInterior(player, tokens);
        }
        if ("nodes".equalsIgnoreCase(root)) {
            return applyNode(player, tokens);
        }
        if ("scene".equalsIgnoreCase(root)) {
            refreshCurrentSurface(player);
            return Optional.of("Scene refreshed in-world.");
        }
        return Optional.empty();
    }

    private Optional<String> applyPlacement(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("castle".equalsIgnoreCase(action)) {
            return Optional.of(placeCastle(player, "Castle site anchored."));
        }
        if ("node".equalsIgnoreCase(action)) {
            return Optional.of(placeNode(player, MinecraftBukkitWorldActionSupport.token(tokens, 3), "Resource node anchored."));
        }
        if ("building".equalsIgnoreCase(action)) {
            return Optional.of(placeBuilding(player, MinecraftBukkitWorldActionSupport.token(tokens, 3), "Construction pad armed."));
        }
        if ("confirm".equalsIgnoreCase(action)) {
            return Optional.of(confirmPlacement(player));
        }
        if ("cancel".equalsIgnoreCase(action)) {
            return Optional.of(cancelPlacement(player));
        }
        if ("preview".equalsIgnoreCase(action) || "status".equalsIgnoreCase(action) || "move".equalsIgnoreCase(action)) {
            return Optional.of(previewPlacement(player, action));
        }
        return Optional.empty();
    }

    private Optional<String> applyBuildings(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("place".equalsIgnoreCase(action)) {
            return Optional.of(placeBuilding(player, MinecraftBukkitWorldActionSupport.token(tokens, 3), "Building foundation placed."));
        }
        if ("stage".equalsIgnoreCase(action) || "spawn".equalsIgnoreCase(action) || "finish".equalsIgnoreCase(action) || "upgrade".equalsIgnoreCase(action)) {
            return Optional.of(stageBuilding(player, MinecraftBukkitWorldActionSupport.token(tokens, 3), action));
        }
        if ("cancel".equalsIgnoreCase(action) || "clear".equalsIgnoreCase(action)) {
            return Optional.of(clearBuilding(player));
        }
        if ("select".equalsIgnoreCase(action) || "status".equalsIgnoreCase(action) || "goto".equalsIgnoreCase(action) || "align".equalsIgnoreCase(action)) {
            return Optional.of(alignToBuilding(player, action));
        }
        return Optional.empty();
    }

    private Optional<String> applyCastle(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("goto".equalsIgnoreCase(action) || "align".equalsIgnoreCase(action) || "open".equalsIgnoreCase(action) || "move".equalsIgnoreCase(action) || "refresh".equalsIgnoreCase(action)) {
            return Optional.of(spawnCastleSite(player, action));
        }
        return Optional.empty();
    }

    private Optional<String> applyInterior(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.of(buildInteriorRoom(player, "Interior room entered."));
        }
        if ("exit".equalsIgnoreCase(action)) {
            return Optional.of(exitInterior(player));
        }
        if ("add".equalsIgnoreCase(action) || "generate".equalsIgnoreCase(action) || "rebuild".equalsIgnoreCase(action) || "regen".equalsIgnoreCase(action) || "move".equalsIgnoreCase(action)) {
            return Optional.of(buildInteriorRoom(player, "Interior " + action + " completed."));
        }
        if ("delete".equalsIgnoreCase(action)) {
            return Optional.of(clearInterior(player));
        }
        return Optional.empty();
    }

    private Optional<String> applyNode(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("place".equalsIgnoreCase(action) || "add".equalsIgnoreCase(action) || "assign".equalsIgnoreCase(action) || "stock".equalsIgnoreCase(action)) {
            return Optional.of(placeNode(player, MinecraftBukkitWorldActionSupport.token(tokens, 3), "Resource node placed."));
        }
        if ("recall".equalsIgnoreCase(action) || "remove".equalsIgnoreCase(action) || "clear".equalsIgnoreCase(action)) {
            return Optional.of(clearNode(player));
        }
        if ("goto".equalsIgnoreCase(action) || "align".equalsIgnoreCase(action) || "select".equalsIgnoreCase(action) || "pillage".equalsIgnoreCase(action)) {
            return Optional.of(nodeFocus(player, action, MinecraftBukkitWorldActionSupport.token(tokens, 3)));
        }
        return Optional.empty();
    }

    private String spawnCastleSite(Player player, String reason) {
        Location previous = castleAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 6.0D, 0.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), previous, 6, 5, 6, CASTLE_TAG);
        }
        castleAnchors.put(player.getUniqueId(), anchor.clone());
        buildCastleFrame(player, anchor);
        getMinecraftBukkitStructureProtectionHandler().registerCastle(player.getUniqueId(), anchor.clone());
        player.getWorld().spawnParticle(Particle.END_ROD, anchor.clone().add(0.5D, 4.0D, 0.5D), 28, 0.6D, 0.8D, 0.6D, 0.02D);
        player.playSound(anchor, Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 1.05f);
        return "Castle site " + reason.toLowerCase(Locale.ROOT);
    }

    private String placeCastle(Player player, String reason) {
        Location previous = castleAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 6.0D, 0.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), previous, 6, 5, 6, CASTLE_TAG);
        }
        castleAnchors.put(player.getUniqueId(), anchor.clone());
        buildCastleFrame(player, anchor);
        getMinecraftBukkitStructureProtectionHandler().registerCastle(player.getUniqueId(), anchor.clone());
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, anchor.clone().add(0.5D, 2.5D, 0.5D), 18, 0.7D, 0.9D, 0.7D, 0.05D);
        player.playSound(anchor, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        return reason;
    }

    private String placeNode(Player player, String nodeType, String reason) {
        String node = normalizedNodeType(nodeType);
        Location previous = nodeAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 4.0D, 0.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), previous, 3, 3, 3, NODE_TAG);
        }
        nodeAnchors.put(player.getUniqueId(), anchor.clone());
        buildNodeFrame(player, anchor, node);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, anchor.clone().add(0.5D, 1.5D, 0.5D), 16, 0.4D, 0.6D, 0.4D, 0.04D);
        player.playSound(anchor, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
        return reason + " " + node + ".";
    }

    private String placeBuilding(Player player, String buildingType, String reason) {
        String building = normalizedBuildingType(buildingType);
        Location previous = buildingAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 5.0D, 0.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), previous, 5, 5, 5, BUILDING_TAG);
        }
        buildingAnchors.put(player.getUniqueId(), anchor.clone());
        buildBuildingFrame(player, anchor, building);
        getMinecraftBukkitStructureProtectionHandler().registerBuilding(player.getUniqueId(), anchor.clone(), building);
        player.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0.5D, 2.5D, 0.5D), 24, 0.8D, 0.9D, 0.8D, 0.04D);
        player.playSound(anchor, Sound.BLOCK_ANVIL_USE, 0.8f, 1.05f);
        return reason + " " + building + ".";
    }

    private String stageBuilding(Player player, String buildingType, String action) {
        String building = normalizedBuildingType(buildingType);
        Location previous = buildingAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 5.0D, 0.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), previous, 5, 5, 5, BUILDING_TAG);
        }
        buildingAnchors.put(player.getUniqueId(), anchor.clone());
        buildBuildingFrame(player, anchor, building);
        getMinecraftBukkitStructureProtectionHandler().registerBuilding(player.getUniqueId(), anchor.clone(), building);
        player.getWorld().spawnParticle(Particle.BLOCK, anchor.clone().add(0.5D, 2.0D, 0.5D), 24, 0.8D, 0.8D, 0.8D, 0.06D, Material.CRAFTING_TABLE.createBlockData());
        player.playSound(anchor, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.0f);
        return "Building " + pastTense(action) + ": " + building + ".";
    }

    private String confirmPlacement(Player player) {
        Location anchor = castleAnchors.get(player.getUniqueId());
        if (anchor == null) {
            anchor = buildingAnchors.get(player.getUniqueId());
        }
        if (anchor == null) {
            return "Nothing to confirm.";
        }
        player.getWorld().spawnParticle(Particle.CLOUD, anchor.clone().add(0.5D, 2.0D, 0.5D), 16, 0.5D, 0.5D, 0.5D, 0.01D);
        player.playSound(anchor, Sound.BLOCK_BELL_USE, 0.9f, 1.0f);
        return "Placement confirmed in-world.";
    }

    private String cancelPlacement(Player player) {
        Location castleAnchor = castleAnchors.remove(player.getUniqueId());
        Location buildingAnchor = buildingAnchors.remove(player.getUniqueId());
        Location nodeAnchor = nodeAnchors.remove(player.getUniqueId());
        getMinecraftBukkitStructureProtectionHandler().clearCastle(player.getUniqueId());
        getMinecraftBukkitStructureProtectionHandler().clearBuilding(player.getUniqueId());
        Location anchor = castleAnchor != null ? castleAnchor : (buildingAnchor != null ? buildingAnchor : nodeAnchor);
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 6, 5, 6, CASTLE_TAG);
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 5, 5, 5, BUILDING_TAG);
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 3, 3, 3, NODE_TAG);
            player.getWorld().spawnParticle(Particle.SMOKE, anchor.clone().add(0.5D, 1.5D, 0.5D), 14, 0.4D, 0.5D, 0.4D, 0.01D);
            player.playSound(anchor, Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 0.9f);
        }
        return "Placement cancelled.";
    }

    private String previewPlacement(Player player, String action) {
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, buildingAnchors.get(player.getUniqueId()), 4.0D, 1.0D);
        player.getWorld().spawnParticle(Particle.BLOCK, anchor.clone().add(0.5D, 0.5D, 0.5D), 12, 0.5D, 0.5D, 0.5D, 0.01D, Material.SMOOTH_STONE.createBlockData());
        player.playSound(anchor, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.0f);
        return "Placement " + action + " shown.";
    }

    private String clearBuilding(Player player) {
        Location anchor = buildingAnchors.remove(player.getUniqueId());
        getMinecraftBukkitStructureProtectionHandler().clearBuilding(player.getUniqueId());
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 5, 5, 5, BUILDING_TAG);
            player.getWorld().spawnParticle(Particle.POOF, anchor.clone().add(0.5D, 1.5D, 0.5D), 18, 0.4D, 0.4D, 0.4D, 0.02D);
            player.playSound(anchor, Sound.BLOCK_GRASS_BREAK, 0.8f, 0.8f);
        }
        return "Building frame cleared.";
    }

    private String alignToBuilding(Player player, String action) {
        Location anchor = buildingAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "Open a building frame first.";
        }
        if ("goto".equalsIgnoreCase(action) || "align".equalsIgnoreCase(action) || "select".equalsIgnoreCase(action)) {
            player.teleport(anchor.clone().add(0.5D, 2.0D, 2.0D));
        }
        player.getWorld().spawnParticle(Particle.END_ROD, anchor.clone().add(0.5D, 2.5D, 0.5D), 12, 0.4D, 0.6D, 0.4D, 0.02D);
        player.playSound(anchor, Sound.ENTITY_ENDERMAN_TELEPORT, 0.75f, 1.15f);
        return "Building " + action + " complete.";
    }

    private String pastTense(String action) {
        if (action == null || action.isBlank()) {
            return "updated";
        }
        String normalized = action.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.endsWith("e")) {
            return normalized + "d";
        }
        return normalized + "ed";
    }

    private String buildInteriorRoom(Player player, String reason) {
        Location previous = interiorAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 3.0D, 1.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), previous, 4, 3, 4, INTERIOR_TAG);
        }
        interiorAnchors.put(player.getUniqueId(), anchor.clone());
        buildInteriorFrame(player, anchor);
        player.teleport(anchor.clone().add(0.5D, 1.0D, 0.5D));
        player.getWorld().spawnParticle(Particle.PORTAL, anchor.clone().add(0.5D, 1.5D, 0.5D), 20, 0.4D, 0.5D, 0.4D, 0.06D);
        player.playSound(anchor, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
        return reason;
    }

    private String exitInterior(Player player) {
        Location anchor = interiorAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No interior instance is active.";
        }
        player.teleport(anchor.clone().add(0.5D, 2.0D, 4.5D));
        player.playSound(anchor, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8f, 1.0f);
        return "Exited interior instance.";
    }

    private String clearInterior(Player player) {
        Location anchor = interiorAnchors.remove(player.getUniqueId());
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 4, 3, 4, INTERIOR_TAG);
            player.getWorld().spawnParticle(Particle.SMOKE, anchor.clone().add(0.5D, 1.5D, 0.5D), 16, 0.3D, 0.4D, 0.3D, 0.02D);
        }
        return "Interior cleared.";
    }

    private String nodeFocus(Player player, String action, String nodeType) {
        Location anchor = nodeAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No resource node is active.";
        }
        if ("goto".equalsIgnoreCase(action) || "align".equalsIgnoreCase(action) || "select".equalsIgnoreCase(action)) {
            player.teleport(anchor.clone().add(0.5D, 1.5D, 2.0D));
        }
        player.getWorld().spawnParticle(Particle.END_ROD, anchor.clone().add(0.5D, 1.0D, 0.5D), 16, 0.3D, 0.4D, 0.3D, 0.03D);
        player.playSound(anchor, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.05f);
        return "Node " + (nodeType == null || nodeType.isBlank() ? "focus" : nodeType) + " updated.";
    }

    private String clearNode(Player player) {
        Location anchor = nodeAnchors.remove(player.getUniqueId());
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 3, 3, 3, NODE_TAG);
            player.getWorld().spawnParticle(Particle.SMOKE, anchor.clone().add(0.5D, 1.0D, 0.5D), 14, 0.3D, 0.4D, 0.3D, 0.02D);
            player.playSound(anchor, Sound.BLOCK_STONE_BREAK, 0.8f, 0.9f);
        }
        return "Resource node cleared.";
    }

    private void refreshCurrentSurface(Player player) {
        Location castleAnchor = castleAnchors.get(player.getUniqueId());
        if (castleAnchor != null) {
            buildCastleFrame(player, castleAnchor);
        }
        Location buildingAnchor = buildingAnchors.get(player.getUniqueId());
        if (buildingAnchor != null) {
            buildBuildingFrame(player, buildingAnchor, "workshop");
        }
        Location nodeAnchor = nodeAnchors.get(player.getUniqueId());
        if (nodeAnchor != null) {
            buildNodeFrame(player, nodeAnchor, "food");
        }
        Location interiorAnchor = interiorAnchors.get(player.getUniqueId());
        if (interiorAnchor != null) {
            buildInteriorFrame(player, interiorAnchor);
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0.0D, 1.0D, 0.0D), 18, 0.8D, 1.0D, 0.8D, 0.06D);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.9f, 1.0f);
    }

    private void buildCastleFrame(Player player, Location anchor) {
        World world = player.getWorld();
        int centerX = anchor.getBlockX();
        int centerY = anchor.getBlockY();
        int centerZ = anchor.getBlockZ();
        MinecraftBukkitWorldActionSupport.clearTrackedEntities(world, anchor, 8.0D, CASTLE_TAG);
        MinecraftBukkitWorldActionSupport.clearTrackedEntities(world, anchor, 8.0D, "tavall.kingdom.hologram");
        MinecraftBukkitWorldActionSupport.fillPad(world, centerX, centerY - 1, centerZ, 5, 5, Material.STONE_BRICKS);
        MinecraftBukkitWorldActionSupport.fillPad(world, centerX, centerY - 2, centerZ, 1, 1, Material.BEACON);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX - 3, centerY, centerZ - 3, 4, Material.COBBLESTONE);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX + 3, centerY, centerZ - 3, 4, Material.COBBLESTONE);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX - 3, centerY, centerZ + 3, 4, Material.COBBLESTONE);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX + 3, centerY, centerZ + 3, 4, Material.COBBLESTONE);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ, Material.GLOWSTONE);
        MinecraftBukkitWorldActionSupport.spawnHologramStack(world, anchor.clone().add(0.5D, 3.0D, 0.5D), java.util.List.of("Kingdom Castle", "Real structure active"));
        spawnCastleMarker(world, anchor);
    }

    private void buildBuildingFrame(Player player, Location anchor, String buildingType) {
        World world = player.getWorld();
        int centerX = anchor.getBlockX();
        int centerY = anchor.getBlockY();
        int centerZ = anchor.getBlockZ();
        MinecraftBukkitWorldActionSupport.clearTrackedEntities(world, anchor, 8.0D, BUILDING_TAG);
        MinecraftBukkitWorldActionSupport.clearTrackedEntities(world, anchor, 8.0D, "tavall.kingdom.hologram");
        Material base = buildingBase(buildingType);
        Material roof = buildingRoof(buildingType);
        MinecraftBukkitWorldActionSupport.fillPad(world, centerX, centerY - 1, centerZ, 4, 4, base);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ, roof);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX + 1, centerY, centerZ, roof);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX - 1, centerY, centerZ, roof);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ + 1, roof);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ - 1, roof);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX - 2, centerY, centerZ - 2, 3, base);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX + 2, centerY, centerZ - 2, 3, base);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX - 2, centerY, centerZ + 2, 3, base);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX + 2, centerY, centerZ + 2, 3, base);
        MinecraftBukkitWorldActionSupport.spawnHologramStack(world, anchor.clone().add(0.5D, 2.5D, 0.5D), java.util.List.of(MinecraftBukkitWorldActionSupport.capitalize(buildingType), "Building frame active"));
        spawnBuildingMarker(world, anchor, buildingType);
    }

    private void buildNodeFrame(Player player, Location anchor, String nodeType) {
        World world = player.getWorld();
        int centerX = anchor.getBlockX();
        int centerY = anchor.getBlockY();
        int centerZ = anchor.getBlockZ();
        Material core;
        if ("wood".equalsIgnoreCase(nodeType)) {
            core = Material.OAK_LOG;
        } else if ("iron".equalsIgnoreCase(nodeType)) {
            core = Material.IRON_BLOCK;
        } else {
            core = Material.EMERALD_BLOCK;
        }
        MinecraftBukkitWorldActionSupport.fillPad(world, centerX, centerY - 1, centerZ, 2, 2, Material.MOSSY_COBBLESTONE);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ, core);
        MinecraftBukkitWorldActionSupport.setColumn(world, centerX, centerY + 1, centerZ, 2, Material.CHAIN);
        MinecraftBukkitWorldActionSupport.spawnHologramStack(world, anchor.clone().add(0.5D, 2.0D, 0.5D), java.util.List.of(MinecraftBukkitWorldActionSupport.capitalize(nodeType) + " Node", "Ready for troop routing"));
    }

    private void buildInteriorFrame(Player player, Location anchor) {
        World world = player.getWorld();
        int centerX = anchor.getBlockX();
        int centerY = anchor.getBlockY();
        int centerZ = anchor.getBlockZ();
        MinecraftBukkitWorldActionSupport.fillPad(world, centerX, centerY - 1, centerZ, 4, 4, Material.POLISHED_ANDESITE);
        for (int x = -3; x <= 3; x++) {
            MinecraftBukkitWorldActionSupport.setBlock(world, centerX + x, centerY, centerZ - 3, Material.SMOOTH_STONE);
            MinecraftBukkitWorldActionSupport.setBlock(world, centerX + x, centerY, centerZ + 3, Material.SMOOTH_STONE);
        }
        for (int z = -3; z <= 3; z++) {
            MinecraftBukkitWorldActionSupport.setBlock(world, centerX - 3, centerY, centerZ + z, Material.SMOOTH_STONE);
            MinecraftBukkitWorldActionSupport.setBlock(world, centerX + 3, centerY, centerZ + z, Material.SMOOTH_STONE);
        }
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ - 3, Material.OAK_DOOR);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY, centerZ + 3, Material.OAK_DOOR);
        MinecraftBukkitWorldActionSupport.setBlock(world, centerX, centerY + 2, centerZ, Material.SEA_LANTERN);
        MinecraftBukkitWorldActionSupport.spawnHologramStack(world, anchor.clone().add(0.5D, 2.5D, 0.5D), java.util.List.of("Kingdom Interior", "Real instance room active"));
    }

    private String normalizedBuildingType(String token) {
        if (token == null || token.isBlank()) {
            return "workshop";
        }
        String normalized = token.toLowerCase(Locale.ROOT);
        if ("farm".equals(normalized) || "farmstead".equals(normalized)) {
            return "farmstead";
        }
        if ("lumber".equals(normalized) || "lumber_mill".equals(normalized) || "lumbermill".equals(normalized)) {
            return "lumber_mill";
        }
        if ("iron".equals(normalized) || "iron_works".equals(normalized) || "ironworks".equals(normalized)) {
            return "iron_works";
        }
        if ("barracks".equals(normalized) || "barrack".equals(normalized)) {
            return "barracks";
        }
        return normalized;
    }

    private String normalizedNodeType(String token) {
        if (token == null || token.isBlank()) {
            return "food";
        }
        String normalized = token.toLowerCase(Locale.ROOT);
        if ("wood".equals(normalized) || "lumber".equals(normalized)) {
            return "wood";
        }
        if ("iron".equals(normalized) || "ore".equals(normalized)) {
            return "iron";
        }
        return "food";
    }

    private Material buildingBase(String buildingType) {
        if ("farmstead".equalsIgnoreCase(buildingType)) {
            return Material.OAK_PLANKS;
        }
        if ("lumber_mill".equalsIgnoreCase(buildingType)) {
            return Material.SPRUCE_LOG;
        }
        if ("iron_works".equalsIgnoreCase(buildingType)) {
            return Material.IRON_BLOCK;
        }
        if ("barracks".equalsIgnoreCase(buildingType)) {
            return Material.STONE_BRICKS;
        }
        return Material.CRAFTING_TABLE;
    }

    private Material buildingRoof(String buildingType) {
        if ("farmstead".equalsIgnoreCase(buildingType)) {
            return Material.HAY_BLOCK;
        }
        if ("lumber_mill".equalsIgnoreCase(buildingType)) {
            return Material.OAK_LOG;
        }
        if ("iron_works".equalsIgnoreCase(buildingType)) {
            return Material.SMITHING_TABLE;
        }
        if ("barracks".equalsIgnoreCase(buildingType)) {
            return Material.DEEPSLATE_BRICKS;
        }
        return Material.BOOKSHELF;
    }

    private void spawnBuildingMarker(World world, Location anchor, String buildingType) {
        ArmorStand marker = (ArmorStand) world.spawnEntity(anchor.clone().add(0.5D, 0.15D, 0.5D), EntityType.ARMOR_STAND);
        marker.setVisible(false);
        marker.setGravity(false);
        marker.setMarker(true);
        marker.setSmall(true);
        marker.setInvulnerable(true);
        marker.setCustomName(MinecraftBukkitWorldActionSupport.capitalize(buildingType));
        marker.setCustomNameVisible(true);
        marker.setCollidable(false);
        marker.addScoreboardTag("tavall.kingdom.helper");
        marker.addScoreboardTag(BUILDING_TAG);
    }

    private void spawnCastleMarker(World world, Location anchor) {
        ArmorStand marker = (ArmorStand) world.spawnEntity(anchor.clone().add(0.5D, 0.15D, 0.5D), EntityType.ARMOR_STAND);
        marker.setVisible(false);
        marker.setGravity(false);
        marker.setMarker(true);
        marker.setSmall(true);
        marker.setInvulnerable(true);
        marker.setCustomName("Castle Keep");
        marker.setCustomNameVisible(true);
        marker.setCollidable(false);
        marker.addScoreboardTag("tavall.kingdom.helper");
        marker.addScoreboardTag(CASTLE_TAG);
    }
}
