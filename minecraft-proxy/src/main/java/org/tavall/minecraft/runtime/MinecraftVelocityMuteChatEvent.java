package org.tavall.minecraft.runtime;

import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.PunishOperationType;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.minecraft.bridge.IMinecraftFrontendBridgeDependencyAccess;
import org.tavall.dependency.IDependencyInjectableConcrete;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MinecraftVelocityMuteChatEvent implements IMinecraftFrontendBridgeDependencyAccess, IDependencyInjectableConcrete {
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        handleChat(event);
    }

    public void handleChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        Map<String, String> context = new LinkedHashMap<>();
        context.put("sourceType", "player");
        context.put("proxy", getMinecraftProxyConfig().serverId());
        context.put("alias", "chat");
        context.put("surfaceIdentity", "VELOCITY_PROXY");
        PunishRequest request = PunishRequest.inspect(
                "minecraft-punish-" + UUID.randomUUID(),
                ResourceGameFrontendPlatform.MINECRAFT,
                player.getUniqueId().toString(),
                player.getUsername(),
                player.getUniqueId().toString(),
                player.getUsername(),
                context,
                Instant.now().toEpochMilli()
        );
        PunishResponse response = getMinecraftControlCommandClient().submitPunishRequest(request);
        if (!response.success() || response.activePunishment() == null) {
            return;
        }
        if (response.activePunishment().operation() != PunishOperationType.MUTE || !response.activePunishment().active()) {
            return;
        }
        String reason = response.activePunishment().reason().isBlank() ? "Not provided" : response.activePunishment().reason();
        String duration = response.activePunishment().durationText().isBlank() ? "Permanent" : response.activePunishment().durationText();
        player.sendMessage(Component.text("You are muted by " + response.activePunishment().senderDisplayName() + ". Duration: " + duration + ". Reason: " + reason + "."));
        event.setResult(PlayerChatEvent.ChatResult.message(""));
    }
}
