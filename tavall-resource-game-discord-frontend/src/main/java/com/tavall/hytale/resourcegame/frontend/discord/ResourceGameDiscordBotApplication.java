package com.tavall.hytale.resourcegame.frontend.discord;

import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionPolicy;
import com.tjxjnoobie.api.platform.global.console.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public final class ResourceGameDiscordBotApplication {
    private ResourceGameDiscordBotApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        DiscordBotConfig config = DiscordBotConfig.fromEnvironment(System.getenv());
        if (!config.hasToken()) {
            throw new IllegalStateException("RESOURCE_GAME_DISCORD_BOT_TOKEN or DISCORD_BOT_TOKEN is required.");
        }
        if (!config.hasControlIngressUrl()) {
            throw new IllegalStateException("RESOURCE_GAME_CONTROL_INGRESS_URL is required.");
        }

        DiscordControlPlaneCommandBridge commandBridge = new DiscordControlPlaneCommandBridge(
                new DiscordKdCommandEnvelopeBridge(),
                new DiscordHttpControlCommandClient(URI.create(config.controlIngressUrl()))
        );
        DiscordPermissionResolver permissionResolver = new DiscordPermissionResolver(config.permissionMapping());
        DiscordCommandPermissionHandler permissionHandler = new DiscordCommandPermissionHandler(
                permissionResolver,
                new UniversalPermissionPolicy()
        );
        DiscordInteractionCommandHandler interactionHandler = new DiscordInteractionCommandHandler(
                commandBridge,
                permissionHandler,
                new DiscordCommandTokenParser()
        );

        JDA jda = JDABuilder.createDefault(config.botToken())
                .setActivity(Activity.playing("Project Novus kingdoms"))
                .addEventListeners(interactionHandler)
                .build()
                .awaitReady();

        new DiscordSlashCommandRegistrar(new DiscordSlashCommandDefinitions()).registerCommands(jda, config.guildId());
        Log.info("Resource game Discord bot started. ingressUrl=" + config.controlIngressUrl());

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
