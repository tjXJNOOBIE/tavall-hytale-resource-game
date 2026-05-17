package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.interaction.InteractionTargetType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MinecraftBukkitInteractionTargetResolver implements IMinecraftBukkitInteractionTargetResolver, IDependencyInjectableConcrete {
    private static final String NPC_TAG = "tavall.kingdom.npc";
    private static final String CASTLE_TAG = "tavall.kingdom.castle";
    private static final String BUILDING_TAG = "tavall.kingdom.building";

    @Override
    public Optional<MinecraftBukkitInteractionTarget> resolve(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        if (entity instanceof Player || entity.getType() == EntityType.PLAYER) {
            return Optional.empty();
        }

        Set<String> scoreboardTags = entity.getScoreboardTags();
        if (scoreboardTags == null || scoreboardTags.isEmpty()) {
            return Optional.empty();
        }

        boolean castleTarget = scoreboardTags.contains(CASTLE_TAG);
        boolean buildingTarget = scoreboardTags.contains(BUILDING_TAG);
        boolean npcTarget = scoreboardTags.contains(NPC_TAG);
        if (!castleTarget && !buildingTarget && !npcTarget) {
            return Optional.empty();
        }

        InteractionTargetType targetType = castleTarget || buildingTarget ? InteractionTargetType.BUILDING : InteractionTargetType.NPC;
        String customName = entity.getCustomName();
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("entityType", entity.getType().name());
        metadata.put("scoreboardTags", String.join(",", scoreboardTags));
        if (customName != null && !customName.isBlank()) {
            metadata.put("customName", customName);
        }
        if (castleTarget) {
            metadata.put("structureKind", "castle");
        } else if (buildingTarget) {
            metadata.put("structureKind", "building");
        }

        World world = entity.getWorld();
        if (world != null) {
            metadata.put("worldName", world.getName());
        }

        if (targetType == InteractionTargetType.BUILDING && customName != null && !customName.isBlank()) {
            metadata.put("buildingType", normalizeToken(customName));
        }
        if (castleTarget && customName != null && !customName.isBlank()) {
            metadata.put("castleType", normalizeToken(customName));
        }
        if (targetType == InteractionTargetType.NPC && customName != null && !customName.isBlank()) {
            metadata.put("npcType", normalizeToken(customName));
        }

        String targetId = entity.getUniqueId() == null ? entity.getType().name().toLowerCase(Locale.ROOT) : entity.getUniqueId().toString();
        String displayName = customName == null || customName.isBlank() ? targetType.name() : customName;
        return Optional.of(new MinecraftBukkitInteractionTarget(targetType, targetId, displayName, metadata));
    }

    private String normalizeToken(String token) {
        String normalized = token.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        return normalized.isBlank() ? token.toLowerCase(Locale.ROOT) : normalized;
    }
}
