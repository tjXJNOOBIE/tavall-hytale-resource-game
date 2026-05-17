package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import net.dv8tion.jda.api.JDA;

public interface IDiscordSlashCommandRegistrar extends IDependencyInjectableInterface {
    void registerCommands(JDA jda, String guildId);
}
