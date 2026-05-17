package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface IDiscordSlashCommandDefinitions extends IDependencyInjectableInterface {
    List<CommandData> commandDefinitions();
}
