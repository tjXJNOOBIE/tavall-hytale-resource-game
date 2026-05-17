package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.platform.global.console.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public final class ResourceGameDiscordBotApplication implements IDiscordFrontendDomain {
    private ResourceGameDiscordBotApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        new ResourceGameDiscordBotApplication().run();
    }

    private void run() throws InterruptedException {
        DiscordBotConfig config = DiscordBotConfig.fromEnvironment(System.getenv());
        if (!config.hasToken()) {
            throw new IllegalStateException("RESOURCE_GAME_DISCORD_BOT_TOKEN or DISCORD_BOT_TOKEN is required.");
        }
        if (!config.hasControlIngressUrl()) {
            throw new IllegalStateException("RESOURCE_GAME_CONTROL_INGRESS_URL is required.");
        }

        new DiscordFrontendDependencyModule().registerDependencies(config);

        JDA jda = JDABuilder.createDefault(getDiscordBotConfig().botToken())
                .setActivity(Activity.playing("Project Novus kingdoms"))
                .addEventListeners(getDiscordInteractionCommandHandler())
                .build()
                .awaitReady();

        getDiscordSlashCommandRegistrar().registerCommands(jda, getDiscordBotConfig().guildId());
        Log.info("Resource game Discord bot started. ingressUrl=" + getDiscordBotConfig().controlIngressUrl());

        CountDownLatch stopLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Log.info("Stopping resource game Discord bot.");
            jda.shutdown();
            stopLatch.countDown();
        }, "resource-game-discord-shutdown"));
        stopLatch.await();
    }

    static ResourceGameDiscordBotApplicationConfigProbe configProbe(Map<String, String> environment) {
        DiscordBotConfig config = DiscordBotConfig.fromEnvironment(environment);
        return new ResourceGameDiscordBotApplicationConfigProbe(
                config.hasToken(),
                config.guildId(),
                config.controlIngressUrl(),
                config.permissionMapping()
        );
    }
}
