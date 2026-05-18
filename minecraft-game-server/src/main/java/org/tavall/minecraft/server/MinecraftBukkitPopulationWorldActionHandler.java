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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MinecraftBukkitPopulationWorldActionHandler implements IMinecraftBukkitPopulationWorldActionHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final String HELPER_TAG = "tavall.kingdom.helper";
    private static final String HOLOGRAM_TAG = "tavall.kingdom.hologram";
    private static final String CITIZEN_TAG = "tavall.kingdom.citizen";
    private static final String COMPANION_TAG = "tavall.kingdom.companion";

    private final ConcurrentMap<UUID, Location> hologramAnchors = new ConcurrentHashMap<UUID, Location>();
    private final ConcurrentMap<UUID, Location> citizenAnchors = new ConcurrentHashMap<UUID, Location>();
    private final ConcurrentMap<UUID, Location> companionAnchors = new ConcurrentHashMap<UUID, Location>();
    private final ConcurrentMap<UUID, Location> resourceAnchors = new ConcurrentHashMap<UUID, Location>();

    @Override
    public Optional<String> apply(Player player, String[] tokens, FrontendCommandVerificationResult result) {
        if (player == null || tokens == null || tokens.length < 2 || result == null || !result.success()) {
            return Optional.empty();
        }
        String root = MinecraftBukkitWorldActionSupport.token(tokens, 1);
        if (root == null) {
            return Optional.empty();
        }
        if ("hologram".equalsIgnoreCase(root) || "holo".equalsIgnoreCase(root)) {
            return applyHologram(player, tokens);
        }
        if ("entity".equalsIgnoreCase(root) || "entities".equalsIgnoreCase(root)) {
            return applyEntity(player, tokens);
        }
        if ("citizens".equalsIgnoreCase(root)) {
            return applyCitizens(player, tokens);
        }
        if ("resources".equalsIgnoreCase(root)) {
            return applyResources(player, tokens);
        }
        if ("companion".equalsIgnoreCase(root)) {
            return applyCompanion(player, tokens);
        }
        if ("account".equalsIgnoreCase(root)) {
            return applyAccount(player, tokens);
        }
        return Optional.empty();
    }

    private Optional<String> applyHologram(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("spawn".equalsIgnoreCase(action)) {
            return Optional.of(spawnHologram(player, tokens, false));
        }
        if ("stack".equalsIgnoreCase(action)) {
            return Optional.of(spawnHologram(player, tokens, true));
        }
        if ("clear".equalsIgnoreCase(action)) {
            return Optional.of(clearHolograms(player));
        }
        if ("status".equalsIgnoreCase(action)) {
            return Optional.of(hologramStatus(player));
        }
        return Optional.empty();
    }

    private Optional<String> applyEntity(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("spawn".equalsIgnoreCase(action)) {
            return Optional.of(spawnEntity(player, MinecraftBukkitWorldActionSupport.token(tokens, 3)));
        }
        if ("clear".equalsIgnoreCase(action)) {
            return Optional.of(clearEntities(player));
        }
        if ("list".equalsIgnoreCase(action)) {
            return Optional.of(listEntities(player));
        }
        return Optional.empty();
    }

    private Optional<String> applyCitizens(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("spawn".equalsIgnoreCase(action) || "create".equalsIgnoreCase(action) || "list".equalsIgnoreCase(action) || "summary".equalsIgnoreCase(action) || "debug".equalsIgnoreCase(action) || "get".equalsIgnoreCase(action)) {
            return Optional.of(spawnCitizenMarker(player, action, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("train".equalsIgnoreCase(action)) {
            return Optional.of(trainCitizenMarker(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("promote".equalsIgnoreCase(action)) {
            return Optional.of(promoteCitizenMarker(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("demote".equalsIgnoreCase(action)) {
            return Optional.of(demoteCitizenMarker(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("setjob".equalsIgnoreCase(action) || "clearjob".equalsIgnoreCase(action) || "age".equalsIgnoreCase(action) || "ageall".equalsIgnoreCase(action) || "health".equalsIgnoreCase(action) || "morale".equalsIgnoreCase(action) || "nutrition".equalsIgnoreCase(action) || "housing".equalsIgnoreCase(action) || "maintenance".equalsIgnoreCase(action) || "refresh-cache".equalsIgnoreCase(action) || "refresh-displays".equalsIgnoreCase(action)) {
            return Optional.of(updateCitizenMarker(player, action, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        return Optional.empty();
    }

    private Optional<String> applyResources(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("give".equalsIgnoreCase(action) || "add".equalsIgnoreCase(action) || "set".equalsIgnoreCase(action)) {
            return Optional.of(applyResourceChange(player, action, MinecraftBukkitWorldActionSupport.token(tokens, 3), MinecraftBukkitWorldActionSupport.token(tokens, 4)));
        }
        return Optional.empty();
    }

    private Optional<String> applyCompanion(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("give".equalsIgnoreCase(action) || "create".equalsIgnoreCase(action)) {
            return Optional.of(spawnCompanion(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("train".equalsIgnoreCase(action)) {
            return Optional.of(trainCompanion(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("claim".equalsIgnoreCase(action)) {
            return Optional.of(claimCompanion(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("cancel".equalsIgnoreCase(action)) {
            return Optional.of(cancelCompanion(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("summon".equalsIgnoreCase(action)) {
            return Optional.of(summonCompanion(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("recall".equalsIgnoreCase(action)) {
            return Optional.of(recallCompanion(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("skill".equalsIgnoreCase(action)) {
            return Optional.of(upgradeCompanionSkill(player, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        if ("wall".equalsIgnoreCase(action)) {
            return Optional.of(applyCompanionWall(player, tokens));
        }
        if ("projection".equalsIgnoreCase(action) || "ui".equalsIgnoreCase(action) || "debug".equalsIgnoreCase(action)) {
            return Optional.of(refreshCompanionSurface(player, action));
        }
        return Optional.empty();
    }

    private Optional<String> applyAccount(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 2);
        if (action == null) {
            return Optional.empty();
        }
        if ("status".equalsIgnoreCase(action)) {
            return Optional.of(showAccountStatus(player));
        }
        if ("addxp".equalsIgnoreCase(action) || "setlevel".equalsIgnoreCase(action) || "debug".equalsIgnoreCase(action)) {
            return Optional.of(applyAccountProgress(player, action, MinecraftBukkitWorldActionSupport.joined(tokens, 3)));
        }
        return Optional.empty();
    }

    private String spawnHologram(Player player, String[] tokens, boolean stack) {
        String label = MinecraftBukkitWorldActionSupport.joined(tokens, 3);
        if (label.isBlank()) {
            label = stack ? "Kingdom stack hologram" : "Kingdom hologram";
        }
        Location previous = hologramAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 4.0D, 2.0D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearTrackedEntities(player.getWorld(), previous, 16.0D, HOLOGRAM_TAG);
        }
        hologramAnchors.put(player.getUniqueId(), anchor.clone());
        if (stack) {
            MinecraftBukkitWorldActionSupport.spawnHologramStack(player.getWorld(), anchor, List.of(label, "Visual stack active", "Use /kd hologram clear"));
        } else {
            MinecraftBukkitWorldActionSupport.spawnHologramStack(player.getWorld(), anchor, List.of(label));
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0.5D, 1.8D, 0.5D), 20, 0.6D, 0.8D, 0.6D, 0.06D);
        player.playSound(anchor, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.1f);
        return "Hologram spawned: " + label + ".";
    }

    private String clearHolograms(Player player) {
        Location anchor = hologramAnchors.remove(player.getUniqueId());
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.clearTrackedEntities(player.getWorld(), anchor, 16.0D, HOLOGRAM_TAG);
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, anchor.clone().add(0.5D, 1.5D, 0.5D), 20, 0.4D, 0.5D, 0.4D, 0.08D);
            player.playSound(anchor, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.7f, 1.2f);
        }
        return "Holograms cleared.";
    }

    private String hologramStatus(Player player) {
        Location anchor = hologramAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No hologram is active.";
        }
        int count = MinecraftBukkitWorldActionSupport.countTaggedEntities(player.getWorld(), anchor, 16.0D, HOLOGRAM_TAG);
        return "Hologram active with " + count + " entity(s).";
    }

    private String spawnEntity(Player player, String token) {
        String entityType = token == null || token.isBlank() ? "farmer" : token.toLowerCase(Locale.ROOT);
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, null, 3.0D, 0.0D);
        Entity entity = spawnEntityAt(player.getWorld(), anchor, entityType, token);
        entity.addScoreboardTag(HELPER_TAG);
        entity.addScoreboardTag("tavall.kingdom.entity");
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, anchor.clone().add(0.5D, 1.0D, 0.5D), 20, 0.4D, 0.6D, 0.4D, 0.03D);
        player.playSound(anchor, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 0.9f, 1.0f);
        return "Entity spawned: " + entityType + ".";
    }

    private String clearEntities(Player player) {
        Location origin = player.getLocation();
        clearTaggedEntities(player.getWorld(), origin, 64.0D, "tavall.kingdom.entity");
        player.getWorld().spawnParticle(Particle.SMOKE, origin.clone().add(0.0D, 1.0D, 0.0D), 20, 0.4D, 0.5D, 0.4D, 0.02D);
        player.playSound(origin, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.8f, 0.9f);
        return "Entity markers cleared.";
    }

    private String listEntities(Player player) {
        int nearby = countTaggedEntities(player.getWorld(), player.getLocation(), 64.0D, "tavall.kingdom.entity");
        return "Nearby kingdom entities: " + nearby + ".";
    }

    private String spawnCitizenMarker(Player player, String action, String payload) {
        String name = payload == null || payload.isBlank() ? "citizen" : payload;
        Location previous = citizenAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 4.0D, 1.0D);
        if (previous != null) {
            clearTaggedEntities(player.getWorld(), previous, 16.0D, CITIZEN_TAG);
        }
        citizenAnchors.put(player.getUniqueId(), anchor.clone());
        Villager villager = (Villager) player.getWorld().spawnEntity(anchor, EntityType.VILLAGER);
        villager.setCustomName(MinecraftBukkitWorldActionSupport.capitalize(name));
        villager.setCustomNameVisible(true);
        villager.addScoreboardTag(HELPER_TAG);
        villager.addScoreboardTag(CITIZEN_TAG);
        villager.addScoreboardTag("tavall.kingdom.npc");
        villager.setPersistent(true);
        villager.setProfession(Villager.Profession.FARMER);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, anchor.clone().add(0.5D, 1.0D, 0.5D), 16, 0.4D, 0.6D, 0.4D, 0.03D);
        player.playSound(anchor, Sound.ENTITY_VILLAGER_YES, 0.8f, 1.1f);
        return "Citizen " + action + "ed: " + name + ".";
    }

    private String trainCitizenMarker(Player player, String payload) {
        Location anchor = citizenAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No citizen marker is active.";
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0.5D, 1.25D, 0.5D), 20, 0.45D, 0.7D, 0.45D, 0.08D);
        player.playSound(anchor, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        return "Citizen training updated.";
    }

    private String promoteCitizenMarker(Player player, String payload) {
        Location anchor = citizenAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No citizen marker is active.";
        }
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, anchor.clone().add(0.5D, 1.0D, 0.5D), 20, 0.45D, 0.7D, 0.45D, 0.02D);
        player.playSound(anchor, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.1f);
        return "Citizen promoted into a troop.";
    }

    private String demoteCitizenMarker(Player player, String payload) {
        Location anchor = citizenAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No citizen marker is active.";
        }
        player.getWorld().spawnParticle(Particle.SMOKE, anchor.clone().add(0.5D, 1.0D, 0.5D), 16, 0.4D, 0.4D, 0.4D, 0.02D);
        player.playSound(anchor, Sound.ENTITY_VILLAGER_NO, 0.9f, 0.85f);
        return "Troop returned to citizen status.";
    }

    private String updateCitizenMarker(Player player, String action, String payload) {
        Location anchor = citizenAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No citizen marker is active.";
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0.5D, 1.0D, 0.5D), 16, 0.4D, 0.6D, 0.4D, 0.04D);
        player.playSound(anchor, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.75f, 1.0f);
        return "Citizen " + action.replace('-', ' ') + " updated.";
    }

    private String applyResourceChange(Player player, String action, String resourceType, String amountToken) {
        int amount = parsePositiveInt(amountToken, 1);
        String resource = normalizedResourceType(resourceType);
        Location previous = resourceAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 3.0D, 1.0D);
        resourceAnchors.put(player.getUniqueId(), anchor.clone());
        if ("set".equalsIgnoreCase(action)) {
            clearResourceItems(player);
        }
        giveResourceItem(player, resource, amount);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().clone().add(0.0D, 1.0D, 0.0D), 16, 0.3D, 0.4D, 0.3D, 0.03D);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.1f);
        return "Resource " + action + "ed: " + resource + " x" + amount + ".";
    }

    private String spawnCompanion(Player player, String payload) {
        String label = payload == null || payload.isBlank() ? "companion" : payload;
        Location previous = companionAnchors.get(player.getUniqueId());
        Location anchor = MinecraftBukkitWorldActionSupport.placeAnchor(player, previous, 4.0D, 1.5D);
        if (previous != null) {
            MinecraftBukkitWorldActionSupport.clearTrackedEntities(player.getWorld(), previous, 16.0D, COMPANION_TAG);
        }
        companionAnchors.put(player.getUniqueId(), anchor.clone());
        Entity entity = player.getWorld().spawnEntity(anchor, EntityType.ALLAY);
        entity.setCustomName(MinecraftBukkitWorldActionSupport.capitalize(label));
        entity.setCustomNameVisible(true);
        entity.addScoreboardTag(HELPER_TAG);
        entity.addScoreboardTag(COMPANION_TAG);
        entity.addScoreboardTag("tavall.kingdom.npc");
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, anchor.clone().add(0.5D, 1.0D, 0.5D), 20, 0.4D, 0.6D, 0.4D, 0.03D);
        player.playSound(anchor, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 1.0f);
        return "Companion created: " + label + ".";
    }

    private String trainCompanion(Player player, String payload) {
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No companion is active.";
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0.5D, 1.0D, 0.5D), 20, 0.45D, 0.6D, 0.45D, 0.08D);
        player.playSound(anchor, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.15f);
        return "Companion training started.";
    }

    private String claimCompanion(Player player, String payload) {
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No companion is active.";
        }
        player.teleport(anchor.clone().add(0.5D, 1.0D, 0.5D));
        player.getWorld().spawnParticle(Particle.PORTAL, anchor.clone().add(0.5D, 1.0D, 0.5D), 20, 0.4D, 0.6D, 0.4D, 0.08D);
        player.playSound(anchor, Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 1.15f);
        return "Companion claimed.";
    }

    private String cancelCompanion(Player player, String payload) {
        Location anchor = companionAnchors.remove(player.getUniqueId());
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.clearTrackedEntities(player.getWorld(), anchor, 16.0D, COMPANION_TAG);
            player.getWorld().spawnParticle(Particle.SMOKE, anchor.clone().add(0.5D, 1.0D, 0.5D), 12, 0.4D, 0.5D, 0.4D, 0.02D);
            player.playSound(anchor, Sound.BLOCK_STONE_BREAK, 0.8f, 0.9f);
        }
        return "Companion cancelled.";
    }

    private String summonCompanion(Player player, String payload) {
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return spawnCompanion(player, payload);
        }
        player.teleport(anchor.clone().add(0.5D, 1.0D, 0.5D));
        player.getWorld().spawnParticle(Particle.PORTAL, anchor.clone().add(0.5D, 1.0D, 0.5D), 24, 0.45D, 0.7D, 0.45D, 0.1D);
        player.playSound(anchor, Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 1.2f);
        return "Companion summoned.";
    }

    private String recallCompanion(Player player, String payload) {
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No companion is active.";
        }
        MinecraftBukkitWorldActionSupport.clearTrackedEntities(player.getWorld(), anchor, 16.0D, COMPANION_TAG);
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, anchor.clone().add(0.5D, 1.0D, 0.5D), 20, 0.4D, 0.5D, 0.4D, 0.08D);
        player.playSound(anchor, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8f, 1.0f);
        return "Companion recalled.";
    }

    private String upgradeCompanionSkill(Player player, String payload) {
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor == null) {
            return "No companion is active.";
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0.5D, 1.25D, 0.5D), 28, 0.45D, 0.7D, 0.45D, 0.08D);
        player.playSound(anchor, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
        return "Companion skill updated.";
    }

    private String applyCompanionWall(Player player, String[] tokens) {
        String action = MinecraftBukkitWorldActionSupport.token(tokens, 3);
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor == null) {
            anchor = player.getLocation().clone();
            companionAnchors.put(player.getUniqueId(), anchor.clone());
        }
        if ("assign".equalsIgnoreCase(action)) {
            MinecraftBukkitWorldActionSupport.fillPad(player.getWorld(), anchor.getBlockX(), anchor.getBlockY(), anchor.getBlockZ(), 2, 1, Material.DEEPSLATE_BRICKS);
            MinecraftBukkitWorldActionSupport.spawnHologramStack(player.getWorld(), anchor.clone().add(0.5D, 2.0D, 0.5D), List.of("Companion wall", "Assigned"));
            player.getWorld().spawnParticle(Particle.BLOCK, anchor.clone().add(0.5D, 1.0D, 0.5D), 16, 0.4D, 0.4D, 0.4D, 0.02D, Material.DEEPSLATE_BRICKS.createBlockData());
            player.playSound(anchor, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.0f);
            return "Companion wall assigned.";
        }
        if ("remove".equalsIgnoreCase(action)) {
            MinecraftBukkitWorldActionSupport.clearCube(player.getWorld(), anchor, 3, 2, 3, COMPANION_TAG);
            player.getWorld().spawnParticle(Particle.SMOKE, anchor.clone().add(0.5D, 1.0D, 0.5D), 12, 0.3D, 0.4D, 0.3D, 0.02D);
            player.playSound(anchor, Sound.BLOCK_STONE_BREAK, 0.8f, 0.9f);
            return "Companion wall removed.";
        }
        if ("debug".equalsIgnoreCase(action)) {
            int nearby = MinecraftBukkitWorldActionSupport.countTaggedEntities(player.getWorld(), anchor, 10.0D, COMPANION_TAG);
            return "Companion wall debug active entities=" + nearby + ".";
        }
        return "Companion wall updated.";
    }

    private String refreshCompanionSurface(Player player, String action) {
        Location anchor = companionAnchors.get(player.getUniqueId());
        if (anchor != null) {
            MinecraftBukkitWorldActionSupport.spawnHologramStack(player.getWorld(), anchor.clone().add(0.5D, 2.0D, 0.5D), List.of("Companion Surface", "Refreshed"));
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().clone().add(0.0D, 1.0D, 0.0D), 18, 0.5D, 0.5D, 0.5D, 0.04D);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.9f, 1.0f);
        return "Companion surface refreshed.";
    }

    private String showAccountStatus(Player player) {
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().clone().add(0.0D, 1.0D, 0.0D), 12, 0.4D, 0.5D, 0.4D, 0.02D);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f);
        return "Account status displayed.";
    }

    private String applyAccountProgress(Player player, String action, String payload) {
        int amount = parsePositiveInt(payload, 1);
        if ("setlevel".equalsIgnoreCase(action)) {
            player.setLevel(Math.max(0, amount));
            player.setTotalExperience(0);
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().clone().add(0.0D, 1.0D, 0.0D), 20, 0.4D, 0.5D, 0.4D, 0.05D);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            return "Account level set to " + amount + ".";
        }
        player.giveExp(Math.max(1, amount));
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().clone().add(0.0D, 1.0D, 0.0D), 16, 0.3D, 0.4D, 0.3D, 0.03D);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.1f);
        return "Account progress updated.";
    }

    private String normalizedResourceType(String token) {
        if (token == null || token.isBlank()) {
            return "food";
        }
        String normalized = token.toLowerCase(Locale.ROOT);
        if ("wood".equals(normalized) || "logs".equals(normalized) || "lumber".equals(normalized)) {
            return "wood";
        }
        if ("iron".equals(normalized) || "ingot".equals(normalized)) {
            return "iron";
        }
        return "food";
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private void clearResourceItems(Player player) {
        player.getInventory().remove(Material.APPLE);
        player.getInventory().remove(Material.OAK_LOG);
        player.getInventory().remove(Material.IRON_INGOT);
        player.getInventory().remove(Material.EMERALD);
        player.getInventory().remove(Material.GOLD_INGOT);
    }

    private void giveResourceItem(Player player, String resource, int amount) {
        Material material;
        if ("wood".equalsIgnoreCase(resource)) {
            material = Material.OAK_LOG;
        } else if ("iron".equalsIgnoreCase(resource)) {
            material = Material.IRON_INGOT;
        } else {
            material = Material.APPLE;
        }
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(material, amount));
    }

    private void clearTaggedEntities(World world, Location center, double radius, String tag) {
        MinecraftBukkitWorldActionSupport.clearTrackedEntities(world, center, radius, tag);
    }

    private int countTaggedEntities(World world, Location center, double radius, String tag) {
        return MinecraftBukkitWorldActionSupport.countTaggedEntities(world, center, radius, tag);
    }

    private Entity spawnEntityAt(World world, Location location, String entityType, String token) {
        if ("farmer".equalsIgnoreCase(entityType)) {
            Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            villager.setCustomName(MinecraftBukkitWorldActionSupport.capitalize(token));
            villager.setCustomNameVisible(true);
            villager.addScoreboardTag(HELPER_TAG);
            villager.addScoreboardTag("tavall.kingdom.entity");
            villager.setPersistent(true);
            villager.setProfession(Villager.Profession.FARMER);
            return villager;
        }
        if ("miner".equalsIgnoreCase(entityType)) {
            Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            villager.setCustomName(MinecraftBukkitWorldActionSupport.capitalize(token));
            villager.setCustomNameVisible(true);
            villager.addScoreboardTag(HELPER_TAG);
            villager.addScoreboardTag("tavall.kingdom.entity");
            villager.setPersistent(true);
            villager.setProfession(Villager.Profession.MASON);
            return villager;
        }
        if ("soldier".equalsIgnoreCase(entityType)) {
            Entity entity = world.spawnEntity(location, EntityType.IRON_GOLEM);
            entity.setCustomName("Kingdom Soldier");
            entity.setCustomNameVisible(true);
            entity.addScoreboardTag(HELPER_TAG);
            entity.addScoreboardTag("tavall.kingdom.entity");
            return entity;
        }
        if ("companion".equalsIgnoreCase(entityType)) {
            Entity entity = world.spawnEntity(location, EntityType.ALLAY);
            entity.setCustomName("Kingdom Companion");
            entity.setCustomNameVisible(true);
            entity.addScoreboardTag(HELPER_TAG);
            entity.addScoreboardTag("tavall.kingdom.entity");
            return entity;
        }
        ArmorStand stand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setCustomName(MinecraftBukkitWorldActionSupport.capitalize(token));
        stand.setCustomNameVisible(true);
        stand.addScoreboardTag(HELPER_TAG);
        stand.addScoreboardTag("tavall.kingdom.entity");
        return stand;
    }
}
