package com.tavall.hytale.resourcegame.shared.frontend;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ResourceGameFrontendActionCatalog {
    public List<ResourceGameFrontendActionDescriptor> actionsFor(ResourceGameFrontendPlatform platform, ResourceGameFrontendObjectKind objectKind) {
        return switch (platform) {
            case HYTALE -> hytaleActionsFor(objectKind);
            case MINECRAFT -> minecraftActionsFor(objectKind);
            case ROBLOX -> robloxActionsFor(objectKind);
            case DISCORD -> discordActionsFor(objectKind);
            case ANDROID, PC -> List.of();
        };
    }

    private List<ResourceGameFrontendActionDescriptor> hytaleActionsFor(ResourceGameFrontendObjectKind objectKind) {
        return switch (objectKind) {
            case CASTLE -> List.of(action("hytale.castle.open", "Open", "HYTALE_UI_ACTION"));
            case RESOURCE_NODE -> List.of(action("hytale.node.interact", "Interact", "HYTALE_ENTITY_INTERACT"));
            case GUILD -> List.of(action("hytale.guild.summary", "Summary", "HYTALE_UI_ACTION"));
            case PETITION -> List.of(action("hytale.petition.bot_verify", "Bot Verify", "HYTALE_BOT_TEST_ACTION", "COUNCIL", Set.of("CREATE_PETITION")));
            case PROPAGANDA_CAMPAIGN -> List.of(action("hytale.propaganda.summary", "Campaign", "HYTALE_UI_ACTION"));
            case KINGDOM_CLOCK -> List.of(action("hytale.clock.view", "Clock", "HYTALE_UI_ACTION"));
            case KINGDOM_SCHEDULE -> List.of(action("hytale.schedule.view", "Schedule", "HYTALE_UI_ACTION"));
            case CITIZEN_POPULATION -> List.of(action("hytale.citizens.summary", "Citizens", "HYTALE_UI_ACTION"));
            case CITIZEN_DISPLAY_ANCHOR -> List.of(action("hytale.citizens.anchor", "Anchor", "HYTALE_CUSTOM_ASSET_VISUAL"));
            case COMPANION -> List.of(
                    action("hytale.companion.overview", "Companion", "HYTALE_UI_ACTION"),
                    action("hytale.companion.train", "Train", "HYTALE_UI_ACTION"),
                    action("hytale.companion.wisdom", "Wisdom Well", "HYTALE_UI_ACTION")
            );
        };
    }

    private List<ResourceGameFrontendActionDescriptor> minecraftActionsFor(ResourceGameFrontendObjectKind objectKind) {
        return switch (objectKind) {
            case CASTLE -> List.of(action("kingdom.castle.info", "Castle", "MINECRAFT_COMMAND"));
            case RESOURCE_NODE -> List.of(action("kingdom.node.collect", "Collect", "MINECRAFT_ENTITY_INTERACT"));
            case GUILD -> List.of(action("kingdom.info", "Info", "MINECRAFT_COMMAND"));
            case PETITION -> List.of(action("kingdom.petition.fund", "Fund", "MINECRAFT_COMMAND", "COUNCIL", Set.of("CREATE_PETITION")));
            case PROPAGANDA_CAMPAIGN -> List.of(action("kingdom.propaganda.info", "Campaign", "MINECRAFT_COMMAND"));
            case KINGDOM_CLOCK -> List.of(action("kingdom.clock.view", "Clock", "MINECRAFT_COMMAND"));
            case KINGDOM_SCHEDULE -> List.of(action("kingdom.schedule.view", "Schedule", "MINECRAFT_COMMAND"));
            case CITIZEN_POPULATION -> List.of(action("kingdom.citizens.summary", "Citizens", "MINECRAFT_COMMAND"));
            case CITIZEN_DISPLAY_ANCHOR -> List.of(action("kingdom.citizens.anchor", "Anchor", "MINECRAFT_RESOURCE_PACK_VISUAL"));
            case COMPANION -> List.of(
                    action("kingdom.companion.summary", "Companion", "MINECRAFT_COMMAND"),
                    action("kingdom.companion.train", "Train", "MINECRAFT_COMMAND"),
                    action("kingdom.companion.wall", "Wall", "MINECRAFT_COMMAND")
            );
        };
    }

    private List<ResourceGameFrontendActionDescriptor> robloxActionsFor(ResourceGameFrontendObjectKind objectKind) {
        return switch (objectKind) {
            case CASTLE -> List.of(action("roblox.castle.prompt", "Castle", "ROBLOX_PROXIMITY_PROMPT"));
            case RESOURCE_NODE -> List.of(action("roblox.node.remote_event", "Node", "ROBLOX_REMOTE_EVENT"));
            case GUILD -> List.of(action("roblox.guild.gui", "Guild", "ROBLOX_GUI_ACTION"));
            case PETITION -> List.of(action("roblox.petition.gui", "Petition", "ROBLOX_GUI_ACTION", "COUNCIL", Set.of("CREATE_PETITION")));
            case PROPAGANDA_CAMPAIGN -> List.of(action("roblox.propaganda.gui", "Campaign", "ROBLOX_GUI_ACTION"));
            case KINGDOM_CLOCK -> List.of(action("roblox.clock.gui", "Clock", "ROBLOX_GUI_ACTION"));
            case KINGDOM_SCHEDULE -> List.of(action("roblox.schedule.gui", "Schedule", "ROBLOX_GUI_ACTION"));
            case CITIZEN_POPULATION -> List.of(action("roblox.citizens.summary", "Citizens", "ROBLOX_GUI_ACTION"));
            case CITIZEN_DISPLAY_ANCHOR -> List.of(action("roblox.citizens.anchor", "Anchor", "ROBLOX_ASSET_VISUAL"));
            case COMPANION -> List.of(
                    action("roblox.companion.gui", "Companion", "ROBLOX_GUI_ACTION"),
                    action("roblox.companion.wisdom", "Wisdom Well", "ROBLOX_GUI_ACTION")
            );
        };
    }

    private List<ResourceGameFrontendActionDescriptor> discordActionsFor(ResourceGameFrontendObjectKind objectKind) {
        return switch (objectKind) {
            case CASTLE -> List.of(action("discord.castle.view", "Castle", "DISCORD_SLASH_COMMAND"));
            case RESOURCE_NODE -> List.of(action("discord.resource.view", "Resource", "DISCORD_SELECT_MENU"));
            case GUILD -> List.of(action("discord.guild.view", "View Guild", "DISCORD_SLASH_COMMAND"));
            case PETITION -> List.of(action("discord.petition.fund", "Fund Petition", "DISCORD_BUTTON", "COUNCIL", Set.of("CREATE_PETITION")));
            case PROPAGANDA_CAMPAIGN -> List.of(action("discord.propaganda.summary", "Campaign", "DISCORD_EMBED_ACTION"));
            case KINGDOM_CLOCK -> List.of(action("discord.clock.view", "Clock", "DISCORD_SLASH_COMMAND"));
            case KINGDOM_SCHEDULE -> List.of(action("discord.schedule.view", "Schedule", "DISCORD_SLASH_COMMAND"));
            case CITIZEN_POPULATION -> List.of(action("discord.citizens.summary", "Citizens", "DISCORD_SLASH_COMMAND"));
            case CITIZEN_DISPLAY_ANCHOR -> List.of(action("discord.citizens.anchor", "Anchor", "DISCORD_EMBED_ACTION"));
            case COMPANION -> List.of(
                    action("discord.companion.summary", "Companion", "DISCORD_SLASH_COMMAND"),
                    action("discord.companion.wall", "Wall", "DISCORD_BUTTON")
            );
        };
    }

    private ResourceGameFrontendActionDescriptor action(String actionId, String label, String interactionType) {
        return new ResourceGameFrontendActionDescriptor(actionId, label, interactionType);
    }

    private ResourceGameFrontendActionDescriptor action(String actionId, String label, String interactionType, String requiredTier, Set<String> requiredPermissions) {
        return new ResourceGameFrontendActionDescriptor(actionId, label, interactionType, Optional.of(requiredTier), requiredPermissions);
    }
}
