package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDiscordBotConfig extends IDependencyInjectableInterface {
    String botToken();

    String guildId();

    String controlIngressUrl();

    DiscordPermissionMapping permissionMapping();

    boolean hasToken();

    boolean hasGuildId();

    boolean hasControlIngressUrl();
}
