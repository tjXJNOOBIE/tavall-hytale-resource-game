package org.tavall.minecraft.runtime;

import org.tavall.api.minecraft.permissions.PunishOperationType;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MinecraftVelocityLoginEvent implements IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    @Subscribe
    public void onLogin(LoginEvent event) {
        handleLogin(event.getPlayer());
    }

    public void handleLogin(Player player) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("sourceType", "player");
        context.put("proxy", getMinecraftProxyConfig().serverId());
        context.put("alias", "login");
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
        if (response.activePunishment().operation() != PunishOperationType.BAN || !response.activePunishment().active()) {
            return;
        }
        String reason = response.activePunishment().reason().isBlank() ? "Not provided" : response.activePunishment().reason();
        String duration = response.activePunishment().durationText().isBlank() ? "Permanent" : response.activePunishment().durationText();
        player.disconnect(Component.text("You were banned by " + response.activePunishment().senderDisplayName() + ". Duration: " + duration + ". Reason: " + reason + "."));
    }
}
