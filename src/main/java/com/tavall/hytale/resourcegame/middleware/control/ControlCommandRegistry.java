package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ControlCommandRegistry {
    private final Map<ControlCommandType, ControlCommandDefinition> definitionsByType;

    public ControlCommandRegistry() {
        EnumMap<ControlCommandType, ControlCommandDefinition> definitions = new EnumMap<>(ControlCommandType.class);
        register(definitions, definition(ControlCommandType.DEBUG_PLAYER_STATE, "Debug Player State", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, true, false,
                arg("universalPlayerId", true, "Universal player ID")));
        register(definitions, definition(ControlCommandType.REGISTER_GLOBAL_ASSET, "Register Global Asset", ControlPermission.MANAGE_GLOBAL_ASSETS, false, CommandTargetScope.GLOBAL, true, true,
                arg("globalAssetId", true, "Canonical asset ID"),
                arg("assetType", true, "Global asset type"),
                arg("displayName", true, "Display name")));
        register(definitions, definition(ControlCommandType.REFRESH_FRONTEND_PROJECTIONS, "Refresh Frontend Projections", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.GLOBAL, false, true,
                arg("platform", false, "Optional target platform"),
                arg("objectType", false, "Optional object type"),
                arg("objectId", false, "Optional object ID")));
        register(definitions, definition(ControlCommandType.ASSIGN_TROOP_WOUND, "Assign Troop Wound", ControlPermission.MANAGE_TROOP_STATE, false, CommandTargetScope.TROOP, true, true,
                arg("troopId", true, "Troop ID"),
                arg("woundType", true, "Wound type"),
                arg("severity", true, "Wound severity")));
        register(definitions, definition(ControlCommandType.START_TROOP_HEALING, "Start Troop Healing", ControlPermission.MANAGE_HEALING_STATE, false, CommandTargetScope.TROOP, true, true,
                arg("universalPlayerId", true, "Resource owner universal player ID"),
                arg("troopId", true, "Troop ID"),
                arg("healingMode", true, "FOOD_ONLY or PROPER_TREATMENT"),
                arg("recipeId", false, "Optional recipe ID"),
                arg("facilityLevel", false, "Optional healing facility level")));
        register(definitions, definition(ControlCommandType.RUN_HEALING_TICK, "Run Healing Tick", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.GLOBAL, false, true,
                arg("tickCount", false, "Optional tick count")));
        register(definitions, definition(ControlCommandType.GIVE_RESOURCE, "Give Resource", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, false,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("globalAssetId", true, "Resource or item global asset ID"),
                arg("amount", true, "Amount to add")));
        register(definitions, definition(ControlCommandType.BROADCAST_PLATFORM_MESSAGE, "Broadcast Platform Message", ControlPermission.BROADCAST_GLOBAL_MESSAGE, true, CommandTargetScope.GLOBAL, true, true,
                arg("message", true, "Message to send"),
                arg("platforms", false, "Optional comma-separated platforms")));
        register(definitions, definition(ControlCommandType.SYNC_PLATFORM_STATE, "Sync Platform State", ControlPermission.EXECUTE_DEBUG_COMMAND, true, CommandTargetScope.PLATFORM, false, true,
                arg("platform", false, "Optional platform or ALL")));
        register(definitions, definition(ControlCommandType.DEBUG_TROOP_HEALING_STATE, "Debug Troop Healing State", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.TROOP, true, false,
                arg("troopId", true, "Troop ID")));
        this.definitionsByType = Map.copyOf(definitions);
    }

    public Optional<ControlCommandDefinition> findDefinition(ControlCommandType commandType) {
        return Optional.ofNullable(definitionsByType.get(commandType));
    }

    public ControlCommandDefinition definition(ControlCommandType commandType) {
        return findDefinition(commandType).orElseThrow(() -> new ControlCommandValidationException("Unknown control command: " + commandType + "."));
    }

    public List<ControlCommandDefinition> definitions() {
        return definitionsByType.values().stream()
                .sorted((left, right) -> left.commandType().name().compareTo(right.commandType().name()))
                .toList();
    }

    private void register(EnumMap<ControlCommandType, ControlCommandDefinition> definitions, ControlCommandDefinition definition) {
        definitions.put(definition.commandType(), definition);
    }

    private ControlCommandDefinition definition(
            ControlCommandType commandType,
            String displayName,
            ControlPermission permission,
            boolean highRisk,
            CommandTargetScope targetScope,
            boolean dryRunSupported,
            boolean fanout,
            ControlCommandArgumentDefinition... arguments
    ) {
        return new ControlCommandDefinition(
                commandType,
                displayName,
                displayName,
                List.of(arguments),
                new ControlCommandPermissionRequirement(permission, highRisk),
                Set.of(targetScope),
                Set.of(GamePlatform.MINECRAFT, GamePlatform.HYTALE, GamePlatform.ROBLOX, GamePlatform.DISCORD),
                dryRunSupported,
                fanout
        );
    }

    private ControlCommandArgumentDefinition arg(String name, boolean required, String description) {
        return new ControlCommandArgumentDefinition(name, required, description);
    }
}
