package org.tavall.minecraft.server.view;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BukkitServerViewAdapter implements MinecraftBukkitServerView, IDependencyInjectableConcrete {
    private final Server server;

    public BukkitServerViewAdapter(Server server) {
        this.server = server;
    }

    @Override
    public String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            return "minecraft-bukkit-host";
        }
    }

    @Override
    public int maxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public Collection<MinecraftBukkitPlayerView> onlinePlayers() {
        List<MinecraftBukkitPlayerView> playerViews = new ArrayList<MinecraftBukkitPlayerView>();
        for (Player player : server.getOnlinePlayers()) {
            playerViews.add(new BukkitPlayerViewAdapter(player));
        }
        return playerViews;
    }
}
