package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDiscordCommandPermissionHandler extends IDependencyInjectableInterface {
    boolean canExecute(DiscordPermissionContext context, DiscordCommandLane commandLane);
}
