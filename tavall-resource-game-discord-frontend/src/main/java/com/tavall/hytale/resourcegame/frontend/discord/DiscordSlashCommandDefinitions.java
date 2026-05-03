package com.tavall.hytale.resourcegame.frontend.discord;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

public final class DiscordSlashCommandDefinitions {
    public static final String USER_COMMAND_NAME = "kingdom";
    public static final String ADMIN_COMMAND_NAME = "kingdom-admin";
    public static final String VERIFY_COMMAND_NAME = "kingdom-verify";
    public static final String COMMAND_OPTION = "command";

    public List<CommandData> commandDefinitions() {
        return List.of(
                Commands.slash(USER_COMMAND_NAME, "Run a resource game player command through the control server.")
                        .addOption(OptionType.STRING, COMMAND_OPTION, "Command text, for example: account debug player-1", true),
                Commands.slash(ADMIN_COMMAND_NAME, "Run a resource game admin command through the control server.")
                        .addOption(OptionType.STRING, COMMAND_OPTION, "Admin command text, for example: resources give player-1 resource.food.rations 10", true),
                Commands.slash(VERIFY_COMMAND_NAME, "Run a live control-plane verification ping.")
        );
    }
}
