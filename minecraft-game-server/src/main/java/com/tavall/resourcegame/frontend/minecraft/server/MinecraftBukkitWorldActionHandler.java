package org.tavall.minecraft.server;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public final class MinecraftBukkitWorldActionHandler implements IMinecraftBukkitWorldActionHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public Optional<String> apply(Player player, String rawInput, FrontendCommandVerificationResult result) {
        if (player == null || rawInput == null || rawInput.isBlank() || result == null || !result.success()) {
            return Optional.empty();
        }
        String normalized = normalize(rawInput);
        String[] tokens = normalized.split("\\s+");
        if (tokens.length < 2) {
            return Optional.empty();
        }
        String root = tokens[1].toLowerCase(Locale.ROOT);
        if ("place".equals(root) || "buildings".equals(root) || "building".equals(root) || "castle".equals(root) || "interior".equals(root) || "nodes".equals(root) || "scene".equals(root)) {
            return getMinecraftBukkitStructureWorldActionHandler().apply(player, tokens, result);
        }
        if ("hologram".equals(root) || "holo".equals(root) || "entity".equals(root) || "entities".equals(root) || "citizens".equals(root) || "resources".equals(root) || "companion".equals(root) || "account".equals(root)) {
            return getMinecraftBukkitPopulationWorldActionHandler().apply(player, tokens, result);
        }
        return Optional.empty();
    }

    private String normalize(String rawInput) {
        String normalizedInput = rawInput.trim();
        while (normalizedInput.startsWith("/")) {
            normalizedInput = normalizedInput.substring(1);
        }
        return normalizedInput;
    }
}
