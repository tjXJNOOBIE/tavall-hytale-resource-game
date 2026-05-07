package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ControlCommandParsingHandler {
    public ControlCommand parseConsoleCommand(String input, ControlOperator operator, CommandIssuedFrom issuedFrom, Instant now) {
        if (input == null || input.isBlank()) {
            throw new ControlCommandValidationException("Command input is required.");
        }
        ArrayList<String> tokens = tokenize(input.trim());
        boolean dryRun = false;
        if (!tokens.isEmpty() && tokens.getFirst().equalsIgnoreCase("dry-run")) {
            dryRun = true;
            tokens.removeFirst();
        }
        if (!tokens.isEmpty() && tokens.getFirst().equalsIgnoreCase("execute")) {
            tokens.removeFirst();
        }
        if (tokens.isEmpty()) {
            throw new ControlCommandValidationException("Command input is required.");
        }
        return switch (tokens.getFirst().toLowerCase()) {
            case "player" -> parsePlayerCommand(tokens, operator, issuedFrom, dryRun, now);
            case "asset" -> parseAssetCommand(tokens, operator, issuedFrom, dryRun, now);
            case "projection" -> parseProjectionCommand(tokens, operator, issuedFrom, dryRun, now);
            case "platform" -> parsePlatformCommand(tokens, operator, issuedFrom, dryRun, now);
            case "troop" -> parseTroopCommand(tokens, operator, issuedFrom, dryRun, now);
            case "kingdom" -> parseKingdomCommand(tokens, operator, issuedFrom, dryRun, now);
            case "clock" -> parseClockCommand(tokens, operator, issuedFrom, dryRun, now);
            case "schedule" -> parseScheduleCommand(tokens, operator, issuedFrom, dryRun, now);
            case "aging" -> parseAgingCommand(tokens, operator, issuedFrom, dryRun, now);
            case "citizen", "citizens" -> parseCitizenCommand(tokens, operator, issuedFrom, dryRun, now);
            case "companion", "companions" -> parseCompanionCommand(tokens, operator, issuedFrom, dryRun, now);
            case "coord", "coordinate" -> parseCoordinateCommand(tokens, operator, issuedFrom, dryRun, now);
            case "instance" -> parseInstanceCommand(tokens, operator, issuedFrom, dryRun, now);
            case "params", "parameter", "parameters" -> parseParameterCommand(tokens, operator, issuedFrom, dryRun, now);
            case "tick" -> parseTickCommand(tokens, operator, issuedFrom, dryRun, now);
            case "resource" -> parseResourceCommand(tokens, operator, issuedFrom, dryRun, now);
            case "broadcast" -> parseBroadcastCommand(tokens, operator, issuedFrom, dryRun, now);
            case "control" -> parseControlCommand(tokens, operator, issuedFrom, dryRun, now);
            case "web-panel", "web", "panel" -> parseImplicitWebPanelCommand(tokens, operator, issuedFrom, dryRun, now);
            default -> throw new ControlCommandValidationException("Unknown console command: " + tokens.getFirst() + ".");
        };
    }

    private ControlCommand parsePlayerCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 3, "player debug <universalPlayerId>");
        if (tokens.get(1).equalsIgnoreCase("location")) {
            requireSize(tokens, 8, "player location <universalPlayerId> <platform> <worldId> <x> <y> <z>");
            return command(ControlCommandType.UPDATE_PLAYER_LOCATION, operator, issuedFrom, CommandTargetScope.PLAYER, parsePlatforms(tokens.get(3)), Map.of(
                    "universalPlayerId", tokens.get(2),
                    "platform", tokens.get(3),
                    "worldId", tokens.get(4),
                    "x", tokens.get(5),
                    "y", tokens.get(6),
                    "z", tokens.get(7)
            ), dryRun, now);
        }
        if (!tokens.get(1).equalsIgnoreCase("debug")) {
            throw new ControlCommandValidationException("Expected player debug <universalPlayerId> or player location <universalPlayerId> <platform> <worldId> <x> <y> <z>.");
        }
        return command(ControlCommandType.DEBUG_PLAYER_STATE, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of("universalPlayerId", tokens.get(2)), dryRun, now);
    }

    private ControlCommand parseAssetCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 5, "asset register <globalAssetId> <assetType> <displayName>");
        if (!tokens.get(1).equalsIgnoreCase("register")) {
            throw new ControlCommandValidationException("Expected asset register <globalAssetId> <assetType> <displayName>.");
        }
        String displayName = String.join(" ", tokens.subList(4, tokens.size()));
        return command(ControlCommandType.REGISTER_GLOBAL_ASSET, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of(
                "globalAssetId", tokens.get(2),
                "assetType", tokens.get(3),
                "displayName", displayName
        ), dryRun, now);
    }

    private ControlCommand parseProjectionCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        if (tokens.size() < 2 || !tokens.get(1).equalsIgnoreCase("refresh")) {
            throw new ControlCommandValidationException("Expected projection refresh [platform].");
        }
        Map<String, String> arguments = new LinkedHashMap<>();
        Set<GamePlatform> platforms = Set.of();
        if (tokens.size() >= 3) {
            arguments.put("platform", tokens.get(2));
            platforms = parsePlatforms(tokens.get(2));
        }
        return command(ControlCommandType.REFRESH_FRONTEND_PROJECTIONS, operator, issuedFrom, platforms.isEmpty() ? CommandTargetScope.GLOBAL : CommandTargetScope.PLATFORM, platforms, arguments, dryRun, now);
    }

    private ControlCommand parsePlatformCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        if (tokens.size() < 2 || !tokens.get(1).equalsIgnoreCase("sync")) {
            throw new ControlCommandValidationException("Expected platform sync <platform|all>.");
        }
        String platform = tokens.size() >= 3 ? tokens.get(2) : "ALL";
        return command(ControlCommandType.SYNC_PLATFORM_STATE, operator, issuedFrom, CommandTargetScope.PLATFORM, parsePlatforms(platform), Map.of("platform", platform), dryRun, now);
    }

    private ControlCommand parseTroopCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 3, "troop <debug|wound|heal> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("debug")) {
            return command(ControlCommandType.DEBUG_TROOP_HEALING_STATE, operator, issuedFrom, CommandTargetScope.TROOP, Set.of(), Map.of("troopId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("wound")) {
            requireSize(tokens, 5, "troop wound <troopId> <woundType> <severity>");
            return command(ControlCommandType.ASSIGN_TROOP_WOUND, operator, issuedFrom, CommandTargetScope.TROOP, Set.of(), Map.of(
                    "troopId", tokens.get(2),
                    "woundType", tokens.get(3),
                    "severity", tokens.get(4)
            ), dryRun, now);
        }
        if (operation.equals("heal")) {
            requireSize(tokens, 5, "troop heal <food|treatment> <universalPlayerId> <troopId> [recipeId] [facilityLevel]");
            String mode = tokens.get(2).equalsIgnoreCase("food") ? "FOOD_ONLY" : "PROPER_TREATMENT";
            LinkedHashMap<String, String> arguments = new LinkedHashMap<>();
            arguments.put("healingMode", mode);
            arguments.put("universalPlayerId", tokens.get(3));
            arguments.put("troopId", tokens.get(4));
            if (tokens.size() >= 6) {
                arguments.put("recipeId", tokens.get(5));
            }
            if (tokens.size() >= 7) {
                arguments.put("facilityLevel", tokens.get(6));
            }
            return command(ControlCommandType.START_TROOP_HEALING, operator, issuedFrom, CommandTargetScope.TROOP, Set.of(), arguments, dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown troop command: " + operation + ".");
    }

    private ControlCommand parseTickCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "tick healing [count]");
        if (tokens.get(1).equalsIgnoreCase("kingdom")) {
            return command(ControlCommandType.RUN_KINGDOM_SIMULATION_TICK, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of(), dryRun, now);
        }
        if (tokens.get(1).equalsIgnoreCase("clock")) {
            if (tokens.size() >= 3 && tokens.get(2).equalsIgnoreCase("all")) {
                return command(ControlCommandType.TICK_ALL_KINGDOM_CLOCKS, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of(), dryRun, now);
            }
            return command(ControlCommandType.TICK_KINGDOM_CLOCK, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        if (!tokens.get(1).equalsIgnoreCase("healing")) {
            throw new ControlCommandValidationException("Expected tick healing [count], tick kingdom, or tick clock [kingdomId|all].");
        }
        String count = tokens.size() >= 3 ? tokens.get(2) : "1";
        return command(ControlCommandType.RUN_HEALING_TICK, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of("tickCount", count), dryRun, now);
    }

    private ControlCommand parseClockCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "clock <state|tick|tick-all|mode|override|clear-override|pause|resume|config|debug|projection> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("state") || operation.equals("debug")) {
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_KINGDOM_CLOCK : ControlCommandType.GET_KINGDOM_CLOCK_STATE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        if (operation.equals("tick")) {
            return command(ControlCommandType.TICK_KINGDOM_CLOCK, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        if (operation.equals("tick-all")) {
            return command(ControlCommandType.TICK_ALL_KINGDOM_CLOCKS, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of(), dryRun, now);
        }
        if (operation.equals("mode")) {
            requireSize(tokens, 4, "clock mode <kingdomId> <mode>");
            return command(ControlCommandType.SET_KINGDOM_CLOCK_MODE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of("kingdomId", tokens.get(2), "mode", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("override")) {
            requireSize(tokens, 4, "clock override <kingdomId> <HH:mm>");
            return command(ControlCommandType.SET_KINGDOM_TIME_OVERRIDE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of("kingdomId", tokens.get(2), "time", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("clear-override")) {
            requireSize(tokens, 3, "clock clear-override <kingdomId>");
            return command(ControlCommandType.CLEAR_KINGDOM_TIME_OVERRIDE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of("kingdomId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("pause") || operation.equals("resume")) {
            return command(operation.equals("pause") ? ControlCommandType.PAUSE_KINGDOM_CLOCK : ControlCommandType.RESUME_KINGDOM_CLOCK, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        if (operation.equals("config")) {
            requireSize(tokens, 3, "clock config <kingdomId> key=value ...");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 3);
            arguments.put("kingdomId", tokens.get(2));
            return command(ControlCommandType.UPDATE_KINGDOM_CLOCK_CONFIG, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("projection")) {
            requireSize(tokens, 3, "clock projection <kingdomId> [platform]");
            return command(ControlCommandType.REFRESH_KINGDOM_CLOCK_PROJECTION, operator, issuedFrom, CommandTargetScope.KINGDOM, parsePlatforms(tokens.size() >= 4 ? tokens.get(3) : "ALL"), Map.of(
                    "kingdomId", tokens.get(2),
                    "platform", tokens.size() >= 4 ? tokens.get(3) : "HYTALE"
            ), dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown clock command: " + operation + ".");
    }

    private ControlCommand parseScheduleCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "schedule <active|create|enable|disable|apply|debug|projection> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("active") || operation.equals("debug")) {
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_KINGDOM_SCHEDULE : ControlCommandType.LIST_ACTIVE_KINGDOM_SCHEDULE_RULES, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        if (operation.equals("create")) {
            requireSize(tokens, 4, "schedule create <kingdomId> <ruleType> key=value ...");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 4);
            arguments.put("kingdomId", tokens.get(2));
            arguments.put("ruleType", tokens.get(3));
            return command(ControlCommandType.CREATE_KINGDOM_SCHEDULE_RULE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("enable") || operation.equals("disable")) {
            requireSize(tokens, 3, "schedule enable <ruleId>");
            return command(operation.equals("enable") ? ControlCommandType.ENABLE_KINGDOM_SCHEDULE_RULE : ControlCommandType.DISABLE_KINGDOM_SCHEDULE_RULE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of("scheduleRuleId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("apply")) {
            return command(ControlCommandType.APPLY_KINGDOM_SCHEDULED_STATE_CHANGES, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        if (operation.equals("projection")) {
            requireSize(tokens, 3, "schedule projection <kingdomId> [platform]");
            return command(ControlCommandType.REFRESH_KINGDOM_SCHEDULE_PROJECTION, operator, issuedFrom, CommandTargetScope.KINGDOM, parsePlatforms(tokens.size() >= 4 ? tokens.get(3) : "ALL"), Map.of(
                    "kingdomId", tokens.get(2),
                    "platform", tokens.size() >= 4 ? tokens.get(3) : "HYTALE"
            ), dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown schedule command: " + operation + ".");
    }

    private ControlCommand parseAgingCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "aging <policy|tick|debug> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("policy")) {
            requireSize(tokens, 3, "aging policy <kingdomId> key=value ...");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 3);
            arguments.put("kingdomId", tokens.get(2));
            return command(ControlCommandType.UPDATE_AGING_TICK_POLICY, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("tick") || operation.equals("debug")) {
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_AGING_TICK : ControlCommandType.RUN_AGING_TICK, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of(
                    "kingdomId", tokens.size() >= 3 ? tokens.get(2) : "kingdom-1"
            ), dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown aging command: " + operation + ".");
    }

    private ControlCommand parseCitizenCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "citizens <spawn|migrate|list|summary|debug|age|ageall|setstage|setjob|clearjob|train|promote|demote|health|morale|nutrition|housing|maintenance|refresh-cache|refresh-displays> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("spawn") || operation.equals("create")) {
            requireSize(tokens, 4, "citizens spawn <ownerPlayerId> <amount> [kingdomId]");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 4);
            arguments.put("ownerPlayerId", tokens.get(2));
            arguments.put("amount", tokens.get(3));
            arguments.putIfAbsent("kingdomId", tokens.size() >= 5 && !tokens.get(4).startsWith("--") && !tokens.get(4).contains("=") ? tokens.get(4) : "kingdom-1");
            return command(ControlCommandType.CREATE_CITIZENS, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("migrate")) {
            requireSize(tokens, 4, "citizens migrate <ownerPlayerId> <amount> [kingdomId]");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 4);
            arguments.put("ownerPlayerId", tokens.get(2));
            arguments.put("amount", tokens.get(3));
            arguments.putIfAbsent("kingdomId", tokens.size() >= 5 && !tokens.get(4).startsWith("--") && !tokens.get(4).contains("=") ? tokens.get(4) : "kingdom-1");
            return command(ControlCommandType.MIGRATE_CITIZEN_IN, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("list") || operation.equals("summary") || operation.equals("refresh-cache") || operation.equals("refresh-displays")) {
            LinkedHashMap<String, String> arguments = scopedCitizenArguments(tokens, 2);
            ControlCommandType type = switch (operation) {
                case "list" -> ControlCommandType.LIST_CITIZENS;
                case "summary" -> ControlCommandType.DEBUG_CITIZEN_SUMMARY;
                case "refresh-cache" -> ControlCommandType.REFRESH_CITIZEN_SUMMARY_CACHE;
                default -> ControlCommandType.REFRESH_CITIZEN_DISPLAY_PROJECTIONS;
            };
            return command(type, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("debug") || operation.equals("get")) {
            requireSize(tokens, 3, "citizens debug <citizenId>");
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_CITIZEN : ControlCommandType.GET_CITIZEN, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of("citizenId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("age")) {
            requireSize(tokens, 4, "citizens age <citizenId> <years>");
            return command(ControlCommandType.DEBUG_SET_CITIZEN_AGE, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of("citizenId", tokens.get(2), "years", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("ageall") || operation.equals("maintenance")) {
            LinkedHashMap<String, String> arguments = scopedCitizenArguments(tokens, 2);
            return command(operation.equals("ageall") ? ControlCommandType.DEBUG_AGE_ALL_CITIZENS : ControlCommandType.RUN_CITIZEN_MAINTENANCE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("setstage")) {
            requireSize(tokens, 4, "citizens setstage <citizenId> <stage>");
            return command(ControlCommandType.UPDATE_CITIZEN_AGE_STAGE, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of("citizenId", tokens.get(2), "ageStage", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("setjob")) {
            requireSize(tokens, 4, "citizens setjob <citizenId> <job>");
            return command(ControlCommandType.ASSIGN_CITIZEN_JOB, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of("citizenId", tokens.get(2), "jobType", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("clearjob") || operation.equals("train") || operation.equals("promote") || operation.equals("demote")) {
            requireSize(tokens, 3, "citizens " + operation + " <citizenId>");
            ControlCommandType type = switch (operation) {
                case "clearjob" -> ControlCommandType.CLEAR_CITIZEN_JOB;
                case "train" -> ControlCommandType.START_CITIZEN_TRAINING;
                case "promote" -> ControlCommandType.PROMOTE_CITIZEN_TO_TROOP;
                default -> ControlCommandType.DEMOTE_TROOP_TO_CITIZEN;
            };
            CommandTargetScope targetScope = operation.equals("promote") || operation.equals("demote") ? CommandTargetScope.TROOP : CommandTargetScope.PLAYER;
            return command(type, operator, issuedFrom, targetScope, Set.of(), Map.of("citizenId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("health") || operation.equals("morale") || operation.equals("nutrition") || operation.equals("housing")) {
            requireSize(tokens, 4, "citizens " + operation + " <citizenId> <state>");
            ControlCommandType type = switch (operation) {
                case "health" -> ControlCommandType.UPDATE_CITIZEN_HEALTH;
                case "morale" -> ControlCommandType.UPDATE_CITIZEN_MORALE;
                case "nutrition" -> ControlCommandType.UPDATE_CITIZEN_NUTRITION;
                default -> ControlCommandType.UPDATE_CITIZEN_HOUSING;
            };
            String key = operation + "State";
            return command(type, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of("citizenId", tokens.get(2), key, tokens.get(3)), dryRun, now);
        }
        if (operation.equals("food-effects") || operation.equals("morale-effects") || operation.equals("night-rest")) {
            LinkedHashMap<String, String> arguments = scopedCitizenArguments(tokens, 2);
            ControlCommandType type = switch (operation) {
                case "food-effects" -> ControlCommandType.APPLY_CITIZEN_FOOD_EFFECTS;
                case "morale-effects" -> ControlCommandType.APPLY_CITIZEN_MORALE_EFFECTS;
                default -> ControlCommandType.APPLY_CITIZEN_NIGHT_REST_EFFECTS;
            };
            return command(type, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown citizens command: " + operation + ".");
    }

    private ControlCommand parseKingdomCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "kingdom <create|debug|scaling|tick|border> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("create")) {
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 2);
            if (tokens.size() >= 3 && !tokens.get(2).startsWith("--") && !tokens.get(2).contains("=")) {
                arguments.putIfAbsent("displayName", tokens.get(2));
            }
            return command(ControlCommandType.CREATE_KINGDOM, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("debug")) {
            requireSize(tokens, 3, "kingdom debug <kingdomId>");
            return command(ControlCommandType.DEBUG_KINGDOM_STATE, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), Map.of("kingdomId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("scaling")) {
            requireSize(tokens, 3, "kingdom scaling evaluate");
            if (!tokens.get(2).equalsIgnoreCase("evaluate")) {
                throw new ControlCommandValidationException("Expected kingdom scaling evaluate.");
            }
            return command(ControlCommandType.EVALUATE_KINGDOM_SCALING, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of(), dryRun, now);
        }
        if (operation.equals("tick")) {
            return command(ControlCommandType.RUN_KINGDOM_SIMULATION_TICK, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of(), dryRun, now);
        }
        if (operation.equals("border")) {
            return parseKingdomBorderCommand(tokens, operator, issuedFrom, dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown kingdom command: " + operation + ".");
    }

    private ControlCommand parseCompanionCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "companion <list|give|debug|setlevel|xp|morale|behavior|train|claim|cancel|skill|summon|recall|wall|projection> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("list")) {
            requireSize(tokens, 3, "companion list <ownerPlayerId>");
            return command(ControlCommandType.LIST_COMPANIONS, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("give") || operation.equals("create")) {
            requireSize(tokens, 4, "companion give <ownerPlayerId> <type>");
            return command(ControlCommandType.CREATE_COMPANION, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(2), "type", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("debug") || operation.equals("get")) {
            requireSize(tokens, 3, "companion debug <companionId>");
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_COMPANION : ControlCommandType.GET_COMPANION, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("companionId", tokens.get(2)), dryRun, now);
        }
        if (operation.equals("setlevel")) {
            requireSize(tokens, 4, "companion setlevel <companionId> <level>");
            return command(ControlCommandType.SET_COMPANION_LEVEL, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("companionId", tokens.get(2), "level", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("xp")) {
            requireSize(tokens, 4, "companion xp <companionId> <amount>");
            return command(ControlCommandType.ADD_COMPANION_XP, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("companionId", tokens.get(2), "xp", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("morale")) {
            requireSize(tokens, 4, "companion morale <companionId> <state>");
            return command(ControlCommandType.UPDATE_COMPANION_MORALE, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("companionId", tokens.get(2), "moraleState", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("behavior")) {
            requireSize(tokens, 4, "companion behavior <companionId> <state>");
            return command(ControlCommandType.SET_COMPANION_BEHAVIOR, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("companionId", tokens.get(2), "behaviorState", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("train") || operation.equals("claim") || operation.equals("cancel") || operation.equals("summon") || operation.equals("recall")) {
            requireSize(tokens, 4, "companion " + operation + " <ownerPlayerId> <companionId>");
            ControlCommandType type = switch (operation) {
                case "train" -> ControlCommandType.START_COMPANION_TRAINING;
                case "claim" -> ControlCommandType.CLAIM_COMPANION_TRAINING;
                case "cancel" -> ControlCommandType.CANCEL_COMPANION_TRAINING;
                case "summon" -> ControlCommandType.SUMMON_COMPANION;
                default -> ControlCommandType.RECALL_COMPANION;
            };
            return command(type, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(2), "companionId", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("skill")) {
            requireSize(tokens, 6, "companion skill <unlock|upgrade> <ownerPlayerId> <companionId> <skillId>");
            ControlCommandType type = tokens.get(2).equalsIgnoreCase("unlock") ? ControlCommandType.UNLOCK_COMPANION_SKILL : ControlCommandType.UPGRADE_COMPANION_SKILL;
            return command(type, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(3), "companionId", tokens.get(4), "skillId", tokens.get(5)), dryRun, now);
        }
        if (operation.equals("wall")) {
            requireSize(tokens, 4, "companion wall <assign|remove|debug> ...");
            if (tokens.get(2).equalsIgnoreCase("debug")) {
                return command(ControlCommandType.DEBUG_COMPANION_WALL, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(3)), dryRun, now);
            }
            if (tokens.get(2).equalsIgnoreCase("assign")) {
                requireSize(tokens, 6, "companion wall assign <ownerPlayerId> <companionId> <section>");
                return command(ControlCommandType.ASSIGN_COMPANION_TO_WALL, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(3), "companionId", tokens.get(4), "wallSectionId", tokens.get(5)), dryRun, now);
            }
            if (tokens.get(2).equalsIgnoreCase("remove")) {
                requireSize(tokens, 5, "companion wall remove <ownerPlayerId> <companionId>");
                return command(ControlCommandType.REMOVE_COMPANION_FROM_WALL, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("ownerPlayerId", tokens.get(3), "companionId", tokens.get(4)), dryRun, now);
            }
            throw new ControlCommandValidationException("Unknown companion wall command: " + tokens.get(2) + ".");
        }
        if (operation.equals("ui") || operation.equals("projection")) {
            requireSize(tokens, 3, "companion projection <companionId>");
            return command(ControlCommandType.REFRESH_COMPANION_PROJECTION, operator, issuedFrom, CommandTargetScope.COMPANION, Set.of(), Map.of("companionId", tokens.get(2)), dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown companion command: " + operation + ".");
    }

    private ControlCommand parseKingdomBorderCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 3, "kingdom border <create|update|debug|resolve|simulate-crossing> ...");
        String operation = tokens.get(2).toLowerCase();
        if (operation.equals("create") || operation.equals("update")) {
            requireSize(tokens, 8, "kingdom border update <kingdomId> <minX> <maxX> <minZ> <maxZ> [worldId]");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 8);
            arguments.put("kingdomId", tokens.get(3));
            arguments.put("minX", tokens.get(4));
            arguments.put("maxX", tokens.get(5));
            arguments.put("minZ", tokens.get(6));
            arguments.put("maxZ", tokens.get(7));
            arguments.putIfAbsent("worldId", tokens.size() >= 9 && !tokens.get(8).startsWith("--") && !tokens.get(8).contains("=") ? tokens.get(8) : "default");
            return command(operation.equals("create") ? ControlCommandType.CREATE_KINGDOM_BORDER : ControlCommandType.UPDATE_KINGDOM_BORDER, operator, issuedFrom, CommandTargetScope.KINGDOM, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("debug") || operation.equals("resolve")) {
            requireSize(tokens, 7, "kingdom border resolve <worldId> <x> <y> <z>");
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_KINGDOM_BORDER : ControlCommandType.RESOLVE_COORDINATE_KINGDOM, operator, issuedFrom, CommandTargetScope.COORDINATE_CONVERSION, Set.of(), Map.of(
                    "worldId", tokens.get(3),
                    "x", tokens.get(4),
                    "y", tokens.get(5),
                    "z", tokens.get(6)
            ), dryRun, now);
        }
        if (operation.equals("simulate-crossing")) {
            requireSize(tokens, 11, "kingdom border simulate-crossing <playerId> <worldId> <fromX> <fromY> <fromZ> <toX> <toY> <toZ>");
            return command(ControlCommandType.SIMULATE_BORDER_CROSSING, operator, issuedFrom, CommandTargetScope.COORDINATE_CONVERSION, Set.of(), Map.of(
                    "universalPlayerId", tokens.get(3),
                    "worldId", tokens.get(4),
                    "fromX", tokens.get(5),
                    "fromY", tokens.get(6),
                    "fromZ", tokens.get(7),
                    "toX", tokens.get(8),
                    "toY", tokens.get(9),
                    "toZ", tokens.get(10)
            ), dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown kingdom border command: " + operation + ".");
    }

    private ControlCommand parseCoordinateCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "coord <convert|debug> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("convert") || operation.equals("debug")) {
            requireSize(tokens, 7, "coord convert <platform> <worldId> <x> <y> <z>");
            return command(operation.equals("debug") ? ControlCommandType.DEBUG_COORDINATE_CONVERSION : ControlCommandType.CONVERT_PLATFORM_COORDINATE, operator, issuedFrom, CommandTargetScope.COORDINATE_CONVERSION, Set.of(), Map.of(
                    "platform", tokens.get(2),
                    "worldId", tokens.get(3),
                    "x", tokens.get(4),
                    "y", tokens.get(5),
                    "z", tokens.get(6)
            ), dryRun, now);
        }
        if (operation.equals("params")) {
            requireSize(tokens, 4, "coord params <platform> key=value ...");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 3);
            arguments.put("platform", tokens.get(2));
            return command(ControlCommandType.UPDATE_COORDINATE_CONVERSION_PARAMETERS, operator, issuedFrom, CommandTargetScope.COORDINATE_CONVERSION, Set.of(), arguments, dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown coordinate command: " + operation + ".");
    }

    private ControlCommand parseInstanceCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "instance <register|health|switch|routing> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("register")) {
            requireSize(tokens, 5, "instance register <platform> <kingdomId> <platformInstanceId> [instanceName]");
            LinkedHashMap<String, String> arguments = namedArguments(tokens, 5);
            arguments.put("platform", tokens.get(2));
            arguments.put("kingdomId", tokens.get(3));
            arguments.put("platformInstanceId", tokens.get(4));
            if (tokens.size() >= 6 && !tokens.get(5).startsWith("--") && !tokens.get(5).contains("=")) {
                arguments.put("instanceName", tokens.get(5));
            }
            return command(ControlCommandType.REGISTER_PLATFORM_INSTANCE, operator, issuedFrom, CommandTargetScope.PLATFORM_INSTANCE, parsePlatforms(tokens.get(2)), arguments, dryRun, now);
        }
        if (operation.equals("health")) {
            requireSize(tokens, 4, "instance health <platformInstanceId> <state>");
            return command(ControlCommandType.UPDATE_PLATFORM_INSTANCE_HEALTH, operator, issuedFrom, CommandTargetScope.PLATFORM_INSTANCE, Set.of(), Map.of("platformInstanceId", tokens.get(2), "state", tokens.get(3)), dryRun, now);
        }
        if (operation.equals("switch")) {
            requireSize(tokens, 6, "instance switch <playerId> <platform> <fromKingdomId> <toKingdomId>");
            return command(ControlCommandType.REQUEST_INSTANCE_SWITCH, operator, issuedFrom, CommandTargetScope.PLAYER, parsePlatforms(tokens.get(3)), Map.of(
                    "universalPlayerId", tokens.get(2),
                    "platform", tokens.get(3),
                    "fromKingdomId", tokens.get(4),
                    "toKingdomId", tokens.get(5)
            ), dryRun, now);
        }
        if (operation.equals("confirm")) {
            requireSize(tokens, 3, "instance confirm <switchRequestId>");
            return command(ControlCommandType.CONFIRM_INSTANCE_SWITCH, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of(
                    "switchRequestId", tokens.get(2)
            ), dryRun, now);
        }
        if (operation.equals("fail")) {
            requireSize(tokens, 4, "instance fail <switchRequestId> <reason>");
            return command(ControlCommandType.FAIL_INSTANCE_SWITCH, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of(
                    "switchRequestId", tokens.get(2),
                    "reason", String.join("_", tokens.subList(3, tokens.size()))
            ), dryRun, now);
        }
        if (operation.equals("routing")) {
            requireSize(tokens, 5, "instance routing debug <kingdomId> <platform>");
            if (!tokens.get(2).equalsIgnoreCase("debug")) {
                throw new ControlCommandValidationException("Expected instance routing debug <kingdomId> <platform>.");
            }
            return command(ControlCommandType.DEBUG_INSTANCE_ROUTING, operator, issuedFrom, CommandTargetScope.PLATFORM_INSTANCE, parsePlatforms(tokens.get(4)), Map.of("kingdomId", tokens.get(3), "platform", tokens.get(4)), dryRun, now);
        }
        throw new ControlCommandValidationException("Unknown instance command: " + operation + ".");
    }

    private ControlCommand parseParameterCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "params <list|get|set|dry-run> ...");
        String operation = tokens.get(1).toLowerCase();
        if (operation.equals("list")) {
            return command(ControlCommandType.LIST_EDITABLE_PARAMETERS, operator, issuedFrom, CommandTargetScope.PARAMETER, Set.of(), Map.of(), dryRun, now);
        }
        if (operation.equals("get")) {
            requireSize(tokens, 3, "params get <parameterKey> [scopeId]");
            LinkedHashMap<String, String> arguments = new LinkedHashMap<>();
            arguments.put("parameterKey", tokens.get(2));
            if (tokens.size() >= 4) {
                arguments.put("scopeId", tokens.get(3));
            }
            return command(ControlCommandType.GET_EDITABLE_PARAMETER, operator, issuedFrom, CommandTargetScope.PARAMETER, Set.of(), arguments, dryRun, now);
        }
        if (operation.equals("set") || operation.equals("dry-run")) {
            requireSize(tokens, 4, "params set <parameterKey> <value> [scopeType] [scopeId]");
            LinkedHashMap<String, String> arguments = new LinkedHashMap<>();
            arguments.put("parameterKey", tokens.get(2));
            arguments.put("value", tokens.get(3));
            arguments.put("scopeType", tokens.size() >= 5 ? tokens.get(4) : "GLOBAL");
            if (tokens.size() >= 6) {
                arguments.put("scopeId", tokens.get(5));
            }
            return command(operation.equals("dry-run") ? ControlCommandType.DRY_RUN_EDITABLE_PARAMETER_UPDATE : ControlCommandType.UPDATE_EDITABLE_PARAMETER, operator, issuedFrom, CommandTargetScope.PARAMETER, Set.of(), arguments, dryRun || operation.equals("dry-run"), now);
        }
        throw new ControlCommandValidationException("Unknown params command: " + operation + ".");
    }

    private ControlCommand parseResourceCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 5, "resource give <universalPlayerId> <globalAssetId> <amount>");
        if (!tokens.get(1).equalsIgnoreCase("give")) {
            throw new ControlCommandValidationException("Expected resource give <universalPlayerId> <globalAssetId> <amount>.");
        }
        return command(ControlCommandType.GIVE_RESOURCE, operator, issuedFrom, CommandTargetScope.PLAYER, Set.of(), Map.of(
                "universalPlayerId", tokens.get(2),
                "globalAssetId", tokens.get(3),
                "amount", tokens.get(4)
        ), dryRun, now);
    }

    private ControlCommand parseBroadcastCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 2, "broadcast <message>");
        String message = String.join(" ", tokens.subList(1, tokens.size()));
        return command(ControlCommandType.BROADCAST_PLATFORM_MESSAGE, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of("message", message), dryRun, now);
    }

    private ControlCommand parseControlCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 3, "control start web-panel [port]");
        ArrayList<String> remainingTokens = new ArrayList<>(tokens.subList(1, tokens.size()));
        if (remainingTokens.getFirst().equalsIgnoreCase("start")) {
            remainingTokens.removeFirst();
            String surface = normalizeControlSurface(remainingTokens.removeFirst());
            return controlSurfaceStartCommand(surface, remainingTokens, operator, issuedFrom, dryRun, now);
        }
        String surface = normalizeControlSurface(remainingTokens.removeFirst());
        if (!remainingTokens.isEmpty() && remainingTokens.getFirst().equalsIgnoreCase("start")) {
            remainingTokens.removeFirst();
            return controlSurfaceStartCommand(surface, remainingTokens, operator, issuedFrom, dryRun, now);
        }
        throw new ControlCommandValidationException("Expected control start web-panel [port] or control web-panel start [port].");
    }

    private ControlCommand parseImplicitWebPanelCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        String surface = normalizeControlSurface(tokens.removeFirst());
        if (tokens.isEmpty() || !tokens.removeFirst().equalsIgnoreCase("start")) {
            throw new ControlCommandValidationException("Expected web-panel start [port].");
        }
        return controlSurfaceStartCommand(surface, tokens, operator, issuedFrom, dryRun, now);
    }

    private ControlCommand controlSurfaceStartCommand(
            String surface,
            ArrayList<String> remainingTokens,
            ControlOperator operator,
            CommandIssuedFrom issuedFrom,
            boolean dryRun,
            Instant now
    ) {
        LinkedHashMap<String, String> arguments = new LinkedHashMap<>();
        arguments.put("surface", surface);
        arguments.put("port", "8080");
        while (!remainingTokens.isEmpty()) {
            String token = remainingTokens.removeFirst();
            if (token.equalsIgnoreCase("--port") || token.equalsIgnoreCase("-p")) {
                if (remainingTokens.isEmpty()) {
                    throw new ControlCommandValidationException("Port value is required.");
                }
                arguments.put("port", remainingTokens.removeFirst());
                continue;
            }
            if (token.matches("\\d+")) {
                arguments.put("port", token);
                continue;
            }
            throw new ControlCommandValidationException("Unknown control surface start argument: " + token + ".");
        }
        return command(ControlCommandType.START_CONTROL_SURFACE, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), arguments, dryRun, now);
    }

    private String normalizeControlSurface(String surface) {
        if (surface.equalsIgnoreCase("web") || surface.equalsIgnoreCase("panel") || surface.equalsIgnoreCase("web-panel") || surface.equalsIgnoreCase("webpanel")) {
            return "web-panel";
        }
        throw new ControlCommandValidationException("Unsupported control surface: " + surface + ".");
    }

    private ControlCommand command(
            ControlCommandType commandType,
            ControlOperator operator,
            CommandIssuedFrom issuedFrom,
            CommandTargetScope targetScope,
            Set<GamePlatform> targetPlatforms,
            Map<String, String> arguments,
            boolean dryRun,
            Instant now
    ) {
        return new ControlCommand(ControlCommandId.random(), commandType, operator, issuedFrom, targetScope, targetPlatforms, arguments, dryRun, now, Map.of());
    }

    private Set<GamePlatform> parsePlatforms(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("ALL")) {
            return Set.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(platform -> !platform.isBlank())
                .map(platform -> GamePlatform.valueOf(platform.toUpperCase()))
                .collect(Collectors.toSet());
    }

    private LinkedHashMap<String, String> namedArguments(ArrayList<String> tokens, int startIndex) {
        LinkedHashMap<String, String> arguments = new LinkedHashMap<>();
        for (int index = startIndex; index < tokens.size(); index++) {
            String token = tokens.get(index);
            if (token.startsWith("--")) {
                String key = token.substring(2);
                if (key.isBlank()) {
                    throw new ControlCommandValidationException("Named argument key is required.");
                }
                if (index + 1 >= tokens.size()) {
                    throw new ControlCommandValidationException("Value is required for argument: " + key + ".");
                }
                arguments.put(key, tokens.get(++index));
                continue;
            }
            int equalsIndex = token.indexOf('=');
            if (equalsIndex > 0) {
                arguments.put(token.substring(0, equalsIndex), token.substring(equalsIndex + 1));
            }
        }
        return arguments;
    }

    private LinkedHashMap<String, String> scopedCitizenArguments(ArrayList<String> tokens, int startIndex) {
        LinkedHashMap<String, String> arguments = namedArguments(tokens, startIndex);
        if (tokens.size() > startIndex && !tokens.get(startIndex).startsWith("--") && !tokens.get(startIndex).contains("=")) {
            String scopeId = tokens.get(startIndex);
            if (scopeId.startsWith("kingdom-")) {
                arguments.put("kingdomId", scopeId);
            } else {
                arguments.put("ownerPlayerId", scopeId);
            }
        }
        arguments.putIfAbsent("kingdomId", "kingdom-1");
        return arguments;
    }

    private void requireSize(ArrayList<String> tokens, int minSize, String usage) {
        if (tokens.size() < minSize) {
            throw new ControlCommandValidationException("Usage: " + usage + ".");
        }
    }

    private ArrayList<String> tokenize(String input) {
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            if (character == '"') {
                quoted = !quoted;
                continue;
            }
            if (Character.isWhitespace(character) && !quoted) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(character);
        }
        if (quoted) {
            throw new ControlCommandValidationException("Unclosed quoted command argument.");
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
