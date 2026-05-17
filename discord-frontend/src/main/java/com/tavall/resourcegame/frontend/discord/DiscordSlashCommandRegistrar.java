package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tjxjnoobie.api.platform.global.console.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public final class DiscordSlashCommandRegistrar implements IDiscordSlashCommandRegistrar, IDiscordFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public void registerCommands(JDA jda, String guildId) {
        if (guildId != null && !guildId.isBlank()) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                Log.warn("Discord guild not available yet for command registration: " + guildId);
                return;
            }
            guild.updateCommands().addCommands(getDiscordSlashCommandDefinitions().commandDefinitions()).queue();
            Log.info("Registered resource game Discord slash commands for guild " + guildId);
            return;
        }
        jda.updateCommands().addCommands(getDiscordSlashCommandDefinitions().commandDefinitions()).queue();
        Log.info("Registered resource game Discord slash commands globally.");
    }
}
