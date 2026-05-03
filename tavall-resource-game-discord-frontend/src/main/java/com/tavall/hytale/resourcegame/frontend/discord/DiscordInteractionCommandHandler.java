package com.tavall.hytale.resourcegame.frontend.discord;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.platform.global.console.Log;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DiscordInteractionCommandHandler extends ListenerAdapter {
    private final DiscordControlPlaneCommandBridge controlPlaneCommandBridge;
    private final DiscordCommandPermissionHandler permissionHandler;
    private final DiscordCommandTokenParser commandTokenParser;

    public DiscordInteractionCommandHandler(
            DiscordControlPlaneCommandBridge controlPlaneCommandBridge,
            DiscordCommandPermissionHandler permissionHandler,
            DiscordCommandTokenParser commandTokenParser
    ) {
        this.controlPlaneCommandBridge = Objects.requireNonNull(controlPlaneCommandBridge, "controlPlaneCommandBridge");
        this.permissionHandler = Objects.requireNonNull(permissionHandler, "permissionHandler");
        this.commandTokenParser = Objects.requireNonNull(commandTokenParser, "commandTokenParser");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        if (DiscordSlashCommandDefinitions.VERIFY_COMMAND_NAME.equals(commandName)) {
            handleVerificationCommand(event);
            return;
        }
        if (DiscordSlashCommandDefinitions.USER_COMMAND_NAME.equals(commandName)) {
            handleControlBackedCommand(event, DiscordCommandLane.USER);
            return;
        }
        if (DiscordSlashCommandDefinitions.ADMIN_COMMAND_NAME.equals(commandName)) {
            handleControlBackedCommand(event, DiscordCommandLane.ADMIN);
        }
    }

    private void handleVerificationCommand(SlashCommandInteractionEvent event) {
        DiscordPermissionContext context = permissionContext(event);
        if (!permissionHandler.canExecute(context, DiscordCommandLane.USER)) {
            event.reply("You do not have permission to run resource game verification commands.").setEphemeral(true).queue();
            return;
        }
        FrontendCommandVerificationResult result = submit(event, List.of("ui", "discord-live-verify"), context);
        event.reply(summaryMessage(result)).setEphemeral(true).queue();
    }

    private void handleControlBackedCommand(SlashCommandInteractionEvent event, DiscordCommandLane commandLane) {
        DiscordPermissionContext context = permissionContext(event);
        if (!permissionHandler.canExecute(context, commandLane)) {
            event.reply("You do not have permission to run " + commandLane.name().toLowerCase() + " resource game commands.").setEphemeral(true).queue();
            return;
        }
        OptionMapping commandOption = event.getOption(DiscordSlashCommandDefinitions.COMMAND_OPTION);
        String commandText = commandOption == null ? "" : commandOption.getAsString();
        List<String> commandTokens = commandTokenParser.parseCommandTokens(commandText);
        if (commandTokens.isEmpty()) {
            event.reply("Command text is required.").setEphemeral(true).queue();
            return;
        }
        FrontendCommandVerificationResult result = submit(event, commandTokens, context);
        event.reply(summaryMessage(result)).setEphemeral(true).queue();
    }

    private FrontendCommandVerificationResult submit(
            SlashCommandInteractionEvent event,
            List<String> commandTokens,
            DiscordPermissionContext context
    ) {
        String guildId = event.getGuild() == null ? "" : event.getGuild().getId();
        String channelId = event.getChannel().getId();
        String correlationId = "discord-" + UUID.randomUUID();
        Log.info("Discord command submitting to resource game control plane correlationId=" + correlationId);
        return controlPlaneCommandBridge.submitKdCommand(
                context.userId(),
                context.displayName(),
                commandTokens,
                correlationId,
                Map.of(
                        "guildId", guildId,
                        "channelId", channelId,
                        "interactionId", event.getId()
                )
        );
    }

    private DiscordPermissionContext permissionContext(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Set<String> roleIds = member == null
                ? Set.of()
                : member.getRoles().stream().map(Role::getId).collect(Collectors.toUnmodifiableSet());
        Set<String> roleNames = member == null
                ? Set.of()
                : member.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
        boolean guildOwner = event.getGuild() != null && event.getUser().getId().equals(event.getGuild().getOwnerId());
        return new DiscordPermissionContext(
                event.getUser().getId(),
                member == null ? event.getUser().getName() : member.getEffectiveName(),
                guildOwner,
                roleIds,
                roleNames
        );
    }

    private String summaryMessage(FrontendCommandVerificationResult result) {
        String commandId = result.controlCommandId() == null ? "none" : result.controlCommandId();
        return "Control verification: " + result.state()
                + "\nSuccess: " + result.success()
                + "\nCommand id: " + commandId
                + "\nMessage: " + result.message();
    }
}
