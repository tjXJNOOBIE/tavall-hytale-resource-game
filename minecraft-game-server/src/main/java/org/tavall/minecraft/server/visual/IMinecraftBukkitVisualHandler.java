package org.tavall.minecraft.server.visual;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.MinecraftVisualRenderRequest;
import org.bukkit.entity.Player;

public interface IMinecraftBukkitVisualHandler extends IDependencyInjectableInterface {
    void renderJoinVisual(Player player, String serverId);

    void renderSnapshotSubmitted(Player player, boolean submitted);

    void renderVisualRequest(Player player, MinecraftVisualRenderRequest request);

    void renderKingdomCommandFeedback(Player player, String commandLine, FrontendCommandVerificationResult result);

    void renderCompanionFeedback(Player player, String commandLine, FrontendCommandVerificationResult result);
}
