package org.tavall.minecraft.server;

import org.tavall.api.minecraft.interaction.InteractionMenuElement;
import org.tavall.api.minecraft.interaction.InteractionMenuModel;
import org.tavall.api.minecraft.interaction.InteractionTargetType;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.api.minecraft.player.PlayerPlatformBindingView;
import org.tavall.api.minecraft.ui.UiActions;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class KingdomAccountGui implements IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        return open(player, "kd account");
    }

    public boolean open(Player player, String sourceCommand) {
        try {
            PlayerDataResponse response = getMinecraftBukkitCommandClientHandler().fetchPlayerData(new PlayerDataRequest(
                    "minecraft-account-ui-" + UUID.randomUUID(),
                    player.getUniqueId(),
                    player.getName(),
                    getMinecraftBukkitServerConfig().serverId(),
                    player.getWorld() == null ? "" : player.getWorld().getName(),
                    Map.of(
                            "sourceCommand", sourceCommand == null || sourceCommand.isBlank() ? "kd account" : sourceCommand,
                            "surfaceIdentity", "BUKKIT_SERVER",
                            "playerName", player.getName()
                    ),
                    Instant.now().toEpochMilli()
            ));
            getMinecraftBukkitInteractionMenuHandler().open(player, toMenu(response), response.message());
        } catch (IOException exception) {
            getMinecraftBukkitLogger().warning("Failed to fetch player data for account UI: " + exception.getMessage());
            player.sendMessage(ChatColor.RED + "Player data unavailable: " + exception.getMessage());
        }
        return true;
    }

    private InteractionMenuModel toMenu(PlayerDataResponse response) {
        List<InteractionMenuElement> elements = new ArrayList<InteractionMenuElement>();
        elements.add(element(10, "PAPER", "Display Name", List.of(response.displayName()), true, null, "", Map.of()));
        elements.add(element(11, "BOOK", "Account State", List.of(
                "Exists: " + (response.accountExists() ? "yes" : "no"),
                "Disabled: " + (response.accountDisabled() ? "yes" : "no")
        ), true, null, "", Map.of()));
        elements.add(element(12, "EXPERIENCE_BOTTLE", "Progression", List.of(
                "Level: " + response.accountLevel(),
                "XP: " + response.accountExperience(),
                "Total XP: " + response.accountTotalExperience()
        ), true, null, "", Map.of()));
        elements.add(element(13, "NAME_TAG", "Platform Bindings", bindingLore(response.platformBindings()), true, null, "", Map.of()));
        elements.add(element(14, "WRITABLE_BOOK", "Primary Email", List.of(
                response.primaryEmail() == null || response.primaryEmail().isBlank() ? "Not linked" : response.primaryEmail()
        ), true, null, "", Map.of()));
        elements.add(element(22, "BARRIER", "Close", List.of("Close this menu."), true, null, UiActions.CLOSE, Map.of()));

        Map<String, String> metadata = new LinkedHashMap<String, String>(response.metadata());
        metadata.put("playerId", response.playerId().toString());
        metadata.put("displayName", response.displayName());
        metadata.put("accountExists", String.valueOf(response.accountExists()));
        metadata.put("bindingCount", String.valueOf(response.platformBindings().size()));

        return new InteractionMenuModel(
                "player-data",
                response.displayName() + " Profile",
                27,
                InteractionTargetType.UNKNOWN,
                response.playerId().toString(),
                elements,
                metadata
        );
    }

    private List<String> bindingLore(List<PlayerPlatformBindingView> bindings) {
        ArrayList<String> lore = new ArrayList<String>();
        if (bindings == null || bindings.isEmpty()) {
            lore.add("No platform bindings.");
            return lore;
        }
        lore.add("Bindings: " + bindings.size());
        for (PlayerPlatformBindingView binding : bindings) {
            lore.add(binding.platform() + ": " + binding.platformDisplayName());
        }
        return lore;
    }

    private InteractionMenuElement element(int slot, String material, String title, List<String> lore, boolean enabled, String disabledReason, String actionId, Map<String, String> metadata) {
        return new InteractionMenuElement(
                "player-data-" + slot,
                slot,
                material,
                title,
                lore,
                enabled,
                disabledReason,
                actionId,
                metadata
        );
    }
}
