package com.tavall.hytale.resourcegame.frontend.discord;

import com.tjxjnoobie.api.platform.global.console.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public final class DiscordSlashCommandRegistrar {
    private final DiscordSlashCommandDefinitions commandDefinitions;

    public DiscordSlashCommandRegistrar(DiscordSlashCommandDefinitions commandDefinitions) {
        this.commandDefinitions = commandDefinitions;
    }

    public void registerCommands(JDA jda, String guildId) {
        if (guildId != null && !guildId.isBlank()) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                Log.warn("Discord guild not available yet for command registration: " + guildId);
                return;
            }
            guild.updateCommands().addCommands(commandDefinitions.commandDefinitions()).queue();
            Log.info("Registered resource game Discord slash commands for guild " + guildId);
            return;
        }
        jda.updateCommands().addCommands(commandDefinitions.commandDefinitions()).queue();
        Log.info("Registered resource game Discord slash commands globally.");
    }
}
