package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;

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
        register(definitions, definition(ControlCommandType.REGISTER_PLATFORM_ASSET_VERSION, "Register Platform Asset Version", ControlPermission.MANAGE_PLATFORM_ASSETS, false, CommandTargetScope.PLATFORM, true, true,
                arg("globalAssetId", true, "Canonical asset ID"),
                arg("platform", true, "Target platform"),
                arg("assetReference", true, "Platform-native asset reference"),
                arg("version", false, "Asset version number"),
                arg("contentHash", false, "Optional content hash")));
        register(definitions, definition(ControlCommandType.CREATE_GUILD, "Create Guild", ControlPermission.MANAGE_GUILD_STATE, true, CommandTargetScope.GUILD, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("name", true, "Guild display name"),
                arg("tag", true, "Guild tag")));
        register(definitions, definition(ControlCommandType.CREATE_CASTLE, "Create Castle", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.CASTLE, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("worldName", false, "Canonical world"),
                arg("x", false, "Canonical X"),
                arg("y", false, "Canonical Y"),
                arg("z", false, "Canonical Z"),
                arg("guildId", false, "Optional owning guild ID")));
        register(definitions, definition(ControlCommandType.CREATE_RESOURCE_NODE, "Create Resource Node", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.RESOURCE_NODE, true, true,
                arg("nodeType", true, "Resource node type"),
                arg("worldName", false, "Canonical world"),
                arg("x", false, "Canonical X"),
                arg("y", false, "Canonical Y"),
                arg("z", false, "Canonical Z"),
                arg("productionRate", false, "Base production rate"),
                arg("ownerGuildId", false, "Optional owning guild ID"),
                arg("ownerPlayerId", false, "Optional owning player ID")));
        register(definitions, definition(ControlCommandType.DEBUG_GUILD_STATE, "Debug Guild State", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.GUILD, false, false,
                arg("guildId", true, "Guild ID")));
        register(definitions, definition(ControlCommandType.DEBUG_CASTLE_STATE, "Debug Castle State", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.CASTLE, false, false,
                arg("castleId", true, "Castle ID")));
        register(definitions, definition(ControlCommandType.RUN_RESOURCE_TICK, "Run Resource Tick", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.RESOURCE_NODE, true, true,
                arg("resourceNodeId", false, "Optional resource node ID")));
        register(definitions, definition(ControlCommandType.RUN_GLOBAL_TICK, "Run Global Tick", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.GLOBAL, true, true));
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
        register(definitions, definition(ControlCommandType.GET_ACCOUNT_STATUS, "Get Account Status", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, true, false,
                arg("universalPlayerId", true, "Universal player ID")));
        register(definitions, definition(ControlCommandType.ADD_ACCOUNT_EXPERIENCE, "Add Account Experience", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("amount", true, "Experience amount to add")));
        register(definitions, definition(ControlCommandType.SET_ACCOUNT_LEVEL, "Set Account Level", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("level", true, "Account level to set")));
        register(definitions, definition(ControlCommandType.SET_ACCOUNT_DEBUG_MODE, "Set Account Debug Mode", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("mode", true, "on, off, or status")));
        register(definitions, definition(ControlCommandType.VERIFY_FRONTEND_ACTION, "Verify Frontend Action", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLATFORM, true, false,
                arg("platform", true, "Frontend platform"),
                arg("surface", true, "Frontend surface"),
                arg("category", true, "Platform command category"),
                arg("input", true, "Raw command or action id")));
        register(definitions, definition(ControlCommandType.ROUTE_FRONTEND_KD_COMMAND, "Route Frontend KD Command", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLATFORM, true, false,
                arg("category", true, "KD command category"),
                arg("rawInput", true, "Original KD command input")));
        register(definitions, definition(ControlCommandType.START_CONTROL_SURFACE, "Start Control Surface", ControlPermission.MANAGE_CONTROL_OPERATORS, true, CommandTargetScope.GLOBAL, true, false,
                arg("surface", true, "Control surface to launch, for example web-panel"),
                arg("port", false, "Optional control surface port")));
        register(definitions, definition(ControlCommandType.CREATE_KINGDOM, "Create Kingdom", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("displayName", false, "Display name"),
                arg("worldId", false, "Canonical world ID"),
                arg("borderSize", false, "Default rectangular border size")));
        register(definitions, definition(ControlCommandType.ARCHIVE_KINGDOM, "Archive Kingdom", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", true, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.DEBUG_KINGDOM_STATE, "Debug Kingdom State", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, true, false,
                arg("kingdomId", true, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.EVALUATE_KINGDOM_SCALING, "Evaluate Kingdom Scaling", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.GLOBAL, true, false));
        register(definitions, definition(ControlCommandType.RUN_KINGDOM_SIMULATION_TICK, "Run Kingdom Simulation Tick", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.GLOBAL, true, true));
        register(definitions, definition(ControlCommandType.CREATE_KINGDOM_BORDER, "Create Kingdom Border", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", true, "Kingdom ID"),
                arg("minX", true, "Minimum canonical X"),
                arg("maxX", true, "Maximum canonical X"),
                arg("minZ", true, "Minimum canonical Z"),
                arg("maxZ", true, "Maximum canonical Z"),
                arg("worldId", false, "Canonical world ID")));
        register(definitions, definition(ControlCommandType.UPDATE_KINGDOM_BORDER, "Update Kingdom Border", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", true, "Kingdom ID"),
                arg("minX", true, "Minimum canonical X"),
                arg("maxX", true, "Maximum canonical X"),
                arg("minZ", true, "Minimum canonical Z"),
                arg("maxZ", true, "Maximum canonical Z"),
                arg("worldId", false, "Canonical world ID")));
        register(definitions, definition(ControlCommandType.DEBUG_KINGDOM_BORDER, "Debug Kingdom Border", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COORDINATE_CONVERSION, true, false,
                arg("worldId", true, "Canonical world ID"),
                arg("x", true, "Canonical X"),
                arg("z", true, "Canonical Z"),
                arg("y", false, "Canonical Y")));
        register(definitions, definition(ControlCommandType.RESOLVE_COORDINATE_KINGDOM, "Resolve Coordinate Kingdom", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COORDINATE_CONVERSION, true, false,
                arg("worldId", true, "Canonical world ID"),
                arg("x", true, "Canonical X"),
                arg("z", true, "Canonical Z"),
                arg("y", false, "Canonical Y")));
        register(definitions, definition(ControlCommandType.SIMULATE_BORDER_CROSSING, "Simulate Border Crossing", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COORDINATE_CONVERSION, true, false,
                arg("worldId", true, "Canonical world ID"),
                arg("fromX", true, "From X"),
                arg("fromZ", true, "From Z"),
                arg("toX", true, "To X"),
                arg("toZ", true, "To Z")));
        register(definitions, definition(ControlCommandType.CONVERT_PLATFORM_COORDINATE, "Convert Platform Coordinate", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COORDINATE_CONVERSION, true, false,
                arg("platform", true, "Platform"),
                arg("worldId", true, "Platform world ID"),
                arg("x", true, "Platform X"),
                arg("z", true, "Platform Z"),
                arg("y", false, "Platform Y")));
        register(definitions, definition(ControlCommandType.UPDATE_COORDINATE_CONVERSION_PARAMETERS, "Update Coordinate Conversion Parameters", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.COORDINATE_CONVERSION, true, true,
                arg("platform", true, "Platform")));
        register(definitions, definition(ControlCommandType.DEBUG_COORDINATE_CONVERSION, "Debug Coordinate Conversion", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COORDINATE_CONVERSION, true, false,
                arg("platform", true, "Platform"),
                arg("worldId", true, "Platform world ID"),
                arg("x", true, "Platform X"),
                arg("z", true, "Platform Z")));
        register(definitions, definition(ControlCommandType.UPDATE_PLAYER_LOCATION, "Update Player Location", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("platform", true, "Platform"),
                arg("worldId", true, "Platform world ID"),
                arg("x", true, "Platform X"),
                arg("z", true, "Platform Z"),
                arg("y", false, "Platform Y")));
        register(definitions, definition(ControlCommandType.DEBUG_PLAYER_KINGDOM_LOCATION, "Debug Player Kingdom Location", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, true, false,
                arg("universalPlayerId", true, "Universal player ID")));
        register(definitions, definition(ControlCommandType.FORCE_PLAYER_KINGDOM_TRANSITION, "Force Player Kingdom Transition", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("fromKingdomId", true, "From kingdom ID"),
                arg("toKingdomId", true, "To kingdom ID"),
                arg("worldId", true, "Canonical world ID"),
                arg("x", true, "Canonical X"),
                arg("z", true, "Canonical Z")));
        register(definitions, definition(ControlCommandType.REGISTER_PLATFORM_INSTANCE, "Register Platform Instance", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.PLATFORM_INSTANCE, true, true,
                arg("platform", true, "Platform"),
                arg("kingdomId", true, "Kingdom ID"),
                arg("platformInstanceId", true, "Platform instance ID")));
        register(definitions, definition(ControlCommandType.UPDATE_PLATFORM_INSTANCE_HEALTH, "Update Platform Instance Health", ControlPermission.MANAGE_KINGDOM_STATE, false, CommandTargetScope.PLATFORM_INSTANCE, true, true,
                arg("platformInstanceId", true, "Platform instance ID"),
                arg("state", true, "Platform instance state")));
        register(definitions, definition(ControlCommandType.UPDATE_KINGDOM_INSTANCE_ROUTING, "Update Kingdom Instance Routing", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.PLATFORM_INSTANCE, true, true,
                arg("kingdomId", true, "Kingdom ID"),
                arg("platform", true, "Platform")));
        register(definitions, definition(ControlCommandType.REQUEST_INSTANCE_SWITCH, "Request Instance Switch", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID"),
                arg("platform", true, "Platform"),
                arg("fromKingdomId", true, "From kingdom ID"),
                arg("toKingdomId", true, "To kingdom ID")));
        register(definitions, definition(ControlCommandType.CONFIRM_INSTANCE_SWITCH, "Confirm Instance Switch", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("switchRequestId", true, "Switch request ID")));
        register(definitions, definition(ControlCommandType.FAIL_INSTANCE_SWITCH, "Fail Instance Switch", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("switchRequestId", true, "Switch request ID")));
        register(definitions, definition(ControlCommandType.DEBUG_INSTANCE_ROUTING, "Debug Instance Routing", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLATFORM_INSTANCE, true, false,
                arg("kingdomId", true, "Kingdom ID"),
                arg("platform", true, "Platform")));
        register(definitions, definition(ControlCommandType.LIST_EDITABLE_PARAMETERS, "List Editable Parameters", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PARAMETER, true, false));
        register(definitions, definition(ControlCommandType.GET_EDITABLE_PARAMETER, "Get Editable Parameter", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PARAMETER, true, false,
                arg("parameterKey", true, "Parameter key")));
        register(definitions, definition(ControlCommandType.UPDATE_EDITABLE_PARAMETER, "Update Editable Parameter", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.PARAMETER, true, true,
                arg("parameterKey", true, "Parameter key"),
                arg("value", true, "Parameter value")));
        register(definitions, definition(ControlCommandType.DRY_RUN_EDITABLE_PARAMETER_UPDATE, "Dry Run Editable Parameter Update", ControlPermission.MANAGE_KINGDOM_STATE, false, CommandTargetScope.PARAMETER, true, false,
                arg("parameterKey", true, "Parameter key"),
                arg("value", true, "Parameter value")));
        register(definitions, definition(ControlCommandType.EVALUATE_NEW_PLAYER_KINGDOM, "Evaluate New Player Kingdom", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, true, false));
        register(definitions, definition(ControlCommandType.ASSIGN_NEW_PLAYER_KINGDOM, "Assign New Player Kingdom", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("universalPlayerId", true, "Universal player ID")));
        register(definitions, definition(ControlCommandType.GET_KINGDOM_CLOCK_STATE, "Get Kingdom Clock State", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, false,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.TICK_KINGDOM_CLOCK, "Tick Kingdom Clock", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.TICK_ALL_KINGDOM_CLOCKS, "Tick All Kingdom Clocks", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.GLOBAL, true, true));
        register(definitions, definition(ControlCommandType.SET_KINGDOM_CLOCK_MODE, "Set Kingdom Clock Mode", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID"),
                arg("mode", true, "REAL_TIME_SYNCED, ACCELERATED, FIXED_OVERRIDE, or PAUSED")));
        register(definitions, definition(ControlCommandType.SET_KINGDOM_TIME_OVERRIDE, "Set Kingdom Time Override", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID"),
                arg("time", true, "HH:mm")));
        register(definitions, definition(ControlCommandType.CLEAR_KINGDOM_TIME_OVERRIDE, "Clear Kingdom Time Override", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.PAUSE_KINGDOM_CLOCK, "Pause Kingdom Clock", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.RESUME_KINGDOM_CLOCK, "Resume Kingdom Clock", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.UPDATE_KINGDOM_CLOCK_CONFIG, "Update Kingdom Clock Config", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.DEBUG_KINGDOM_CLOCK, "Debug Kingdom Clock", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, false,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.CREATE_KINGDOM_SCHEDULE_RULE, "Create Kingdom Schedule Rule", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID"),
                arg("ruleType", true, "Schedule rule type")));
        register(definitions, definition(ControlCommandType.UPDATE_KINGDOM_SCHEDULE_RULE, "Update Kingdom Schedule Rule", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("scheduleRuleId", true, "Schedule rule ID")));
        register(definitions, definition(ControlCommandType.ENABLE_KINGDOM_SCHEDULE_RULE, "Enable Kingdom Schedule Rule", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("scheduleRuleId", true, "Schedule rule ID")));
        register(definitions, definition(ControlCommandType.DISABLE_KINGDOM_SCHEDULE_RULE, "Disable Kingdom Schedule Rule", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("scheduleRuleId", true, "Schedule rule ID")));
        register(definitions, definition(ControlCommandType.LIST_ACTIVE_KINGDOM_SCHEDULE_RULES, "List Active Kingdom Schedule Rules", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, false,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.APPLY_KINGDOM_SCHEDULED_STATE_CHANGES, "Apply Kingdom Scheduled State Changes", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.DEBUG_KINGDOM_SCHEDULE, "Debug Kingdom Schedule", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, false,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.UPDATE_AGING_TICK_POLICY, "Update Aging Tick Policy", ControlPermission.MANAGE_KINGDOM_STATE, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.RUN_AGING_TICK, "Run Aging Tick", ControlPermission.EXECUTE_GLOBAL_TICK, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.DEBUG_AGING_TICK, "Debug Aging Tick", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, false,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.REFRESH_KINGDOM_CLOCK_PROJECTION, "Refresh Kingdom Clock Projection", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, true,
                arg("kingdomId", false, "Kingdom ID"),
                arg("platform", false, "Platform")));
        register(definitions, definition(ControlCommandType.REFRESH_KINGDOM_SCHEDULE_PROJECTION, "Refresh Kingdom Schedule Projection", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, true,
                arg("kingdomId", false, "Kingdom ID"),
                arg("platform", false, "Platform")));
        register(definitions, definition(ControlCommandType.CREATE_CITIZEN, "Create Citizen", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.CREATE_CITIZENS, "Create Citizens", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("amount", true, "Citizen amount"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.MIGRATE_CITIZEN_IN, "Migrate Citizen In", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("amount", false, "Citizen amount"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.LIST_CITIZENS, "List Citizens", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, false, false,
                arg("ownerPlayerId", false, "Owner universal player ID"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.GET_CITIZEN, "Get Citizen", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, false, false,
                arg("citizenId", true, "Citizen ID")));
        register(definitions, definition(ControlCommandType.DEBUG_CITIZEN, "Debug Citizen", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, false, false,
                arg("citizenId", true, "Citizen ID")));
        register(definitions, definition(ControlCommandType.DEBUG_CITIZEN_SUMMARY, "Debug Citizen Summary", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.PLAYER, false, false,
                arg("ownerPlayerId", false, "Owner universal player ID"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.UPDATE_CITIZEN_AGE_STAGE, "Update Citizen Age Stage", ControlPermission.MANAGE_PLAYER_STATE, true, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("ageStage", true, "Age stage")));
        register(definitions, definition(ControlCommandType.DEBUG_SET_CITIZEN_AGE, "Debug Set Citizen Age", ControlPermission.EXECUTE_DEBUG_COMMAND, true, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("years", true, "Derived citizen years")));
        register(definitions, definition(ControlCommandType.DEBUG_AGE_ALL_CITIZENS, "Debug Age All Citizens", ControlPermission.EXECUTE_DEBUG_COMMAND, true, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.ASSIGN_CITIZEN_JOB, "Assign Citizen Job", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("jobType", true, "Existing CitizenJobType value")));
        register(definitions, definition(ControlCommandType.CLEAR_CITIZEN_JOB, "Clear Citizen Job", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID")));
        register(definitions, definition(ControlCommandType.START_CITIZEN_TRAINING, "Start Citizen Training", ControlPermission.MANAGE_TROOP_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID")));
        register(definitions, definition(ControlCommandType.PROMOTE_CITIZEN_TO_TROOP, "Promote Citizen To Troop", ControlPermission.MANAGE_TROOP_STATE, true, CommandTargetScope.TROOP, true, true,
                arg("citizenId", true, "Citizen ID")));
        register(definitions, definition(ControlCommandType.DEMOTE_TROOP_TO_CITIZEN, "Demote Troop To Citizen", ControlPermission.MANAGE_TROOP_STATE, true, CommandTargetScope.TROOP, true, true,
                arg("citizenId", true, "Citizen ID")));
        register(definitions, definition(ControlCommandType.UPDATE_CITIZEN_HEALTH, "Update Citizen Health", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("healthState", true, "Health state")));
        register(definitions, definition(ControlCommandType.UPDATE_CITIZEN_MORALE, "Update Citizen Morale", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("moraleState", true, "Morale state")));
        register(definitions, definition(ControlCommandType.UPDATE_CITIZEN_NUTRITION, "Update Citizen Nutrition", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("nutritionState", true, "Nutrition state")));
        register(definitions, definition(ControlCommandType.UPDATE_CITIZEN_HOUSING, "Update Citizen Housing", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.PLAYER, true, true,
                arg("citizenId", true, "Citizen ID"),
                arg("housingState", true, "Housing state")));
        register(definitions, definition(ControlCommandType.RUN_CITIZEN_MAINTENANCE, "Run Citizen Maintenance", ControlPermission.EXECUTE_GLOBAL_TICK, false, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.APPLY_CITIZEN_FOOD_EFFECTS, "Apply Citizen Food Effects", ControlPermission.EXECUTE_GLOBAL_TICK, false, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.APPLY_CITIZEN_MORALE_EFFECTS, "Apply Citizen Morale Effects", ControlPermission.EXECUTE_GLOBAL_TICK, false, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.APPLY_CITIZEN_NIGHT_REST_EFFECTS, "Apply Citizen Night Rest Effects", ControlPermission.EXECUTE_GLOBAL_TICK, false, CommandTargetScope.KINGDOM, true, true,
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.REFRESH_CITIZEN_SUMMARY_CACHE, "Refresh Citizen Summary Cache", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, true,
                arg("ownerPlayerId", false, "Owner universal player ID"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.REFRESH_CITIZEN_DISPLAY_PROJECTIONS, "Refresh Citizen Display Projections", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.KINGDOM, false, true,
                arg("ownerPlayerId", false, "Owner universal player ID"),
                arg("kingdomId", false, "Kingdom ID")));
        register(definitions, definition(ControlCommandType.CREATE_COMPANION, "Create Companion", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("type", true, "HEALER, BRAWLER, BRUTE, or ARCANE")));
        register(definitions, definition(ControlCommandType.LIST_COMPANIONS, "List Companions", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COMPANION, false, false,
                arg("ownerPlayerId", true, "Owner universal player ID")));
        register(definitions, definition(ControlCommandType.GET_COMPANION, "Get Companion", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COMPANION, false, false,
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.DEBUG_COMPANION, "Debug Companion", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COMPANION, false, false,
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.SET_COMPANION_LEVEL, "Set Companion Level", ControlPermission.EXECUTE_DEBUG_COMMAND, true, CommandTargetScope.COMPANION, true, true,
                arg("companionId", true, "Companion ID"),
                arg("level", true, "Level 1-70")));
        register(definitions, definition(ControlCommandType.ADD_COMPANION_XP, "Add Companion XP", ControlPermission.EXECUTE_DEBUG_COMMAND, true, CommandTargetScope.COMPANION, true, true,
                arg("companionId", true, "Companion ID"),
                arg("xp", true, "XP to add")));
        register(definitions, definition(ControlCommandType.UPDATE_COMPANION_MORALE, "Update Companion Morale", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("companionId", true, "Companion ID"),
                arg("moraleState", true, "HIGH, MEDIUM, LOW, or POOR")));
        register(definitions, definition(ControlCommandType.SET_COMPANION_BEHAVIOR, "Set Companion Behavior", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("companionId", true, "Companion ID"),
                arg("behaviorState", true, "IDLE, FOLLOWING, AGGRESSIVE, FLEEING, or DUELING")));
        register(definitions, definition(ControlCommandType.START_COMPANION_TRAINING, "Start Companion Training", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.CLAIM_COMPANION_TRAINING, "Claim Companion Training", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.CANCEL_COMPANION_TRAINING, "Cancel Companion Training", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.UNLOCK_COMPANION_SKILL, "Unlock Companion Skill", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID"),
                arg("skillId", true, "Skill ID or name")));
        register(definitions, definition(ControlCommandType.UPGRADE_COMPANION_SKILL, "Upgrade Companion Skill", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID"),
                arg("skillId", true, "Skill ID or name")));
        register(definitions, definition(ControlCommandType.SUMMON_COMPANION, "Summon Companion", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.RECALL_COMPANION, "Recall Companion", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.ASSIGN_COMPANION_TO_WALL, "Assign Companion To Wall", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID"),
                arg("wallSectionId", true, "Wall section ID")));
        register(definitions, definition(ControlCommandType.REMOVE_COMPANION_FROM_WALL, "Remove Companion From Wall", ControlPermission.MANAGE_PLAYER_STATE, false, CommandTargetScope.COMPANION, true, true,
                arg("ownerPlayerId", true, "Owner universal player ID"),
                arg("companionId", true, "Companion ID")));
        register(definitions, definition(ControlCommandType.DEBUG_COMPANION_WALL, "Debug Companion Wall", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COMPANION, false, false,
                arg("ownerPlayerId", true, "Owner universal player ID")));
        register(definitions, definition(ControlCommandType.REFRESH_COMPANION_PROJECTION, "Refresh Companion Projection", ControlPermission.EXECUTE_DEBUG_COMMAND, false, CommandTargetScope.COMPANION, false, true,
                arg("companionId", true, "Companion ID")));
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
                Set.of(GamePlatform.MINECRAFT, GamePlatform.HYTALE, GamePlatform.ROBLOX, GamePlatform.DISCORD, GamePlatform.ANDROID, GamePlatform.PC),
                dryRunSupported,
                fanout
        );
    }

    private ControlCommandArgumentDefinition arg(String name, boolean required, String description) {
        return new ControlCommandArgumentDefinition(name, required, description);
    }
}
