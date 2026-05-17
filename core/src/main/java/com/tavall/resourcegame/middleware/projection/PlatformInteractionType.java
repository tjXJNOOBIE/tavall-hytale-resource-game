package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.common.GamePlatform;

public enum PlatformInteractionType {
    MINECRAFT_COMMAND(GamePlatform.MINECRAFT),
    MINECRAFT_ENTITY_INTERACT(GamePlatform.MINECRAFT),
    MINECRAFT_INVENTORY_CLICK(GamePlatform.MINECRAFT),
    MINECRAFT_RESOURCE_PACK_VISUAL(GamePlatform.MINECRAFT),
    HYTALE_ENTITY_INTERACT(GamePlatform.HYTALE),
    HYTALE_UI_ACTION(GamePlatform.HYTALE),
    HYTALE_BOT_TEST_ACTION(GamePlatform.HYTALE),
    HYTALE_CUSTOM_ASSET_VISUAL(GamePlatform.HYTALE),
    ROBLOX_REMOTE_EVENT(GamePlatform.ROBLOX),
    ROBLOX_PROXIMITY_PROMPT(GamePlatform.ROBLOX),
    ROBLOX_GUI_ACTION(GamePlatform.ROBLOX),
    ROBLOX_ASSET_VISUAL(GamePlatform.ROBLOX),
    DISCORD_SLASH_COMMAND(GamePlatform.DISCORD),
    DISCORD_BUTTON(GamePlatform.DISCORD),
    DISCORD_SELECT_MENU(GamePlatform.DISCORD),
    DISCORD_EMBED_ACTION(GamePlatform.DISCORD);

    private final GamePlatform platform;

    PlatformInteractionType(GamePlatform platform) {
        this.platform = platform;
    }

    public GamePlatform platform() {
        return platform;
    }
}
