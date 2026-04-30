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
            case "tick" -> parseTickCommand(tokens, operator, issuedFrom, dryRun, now);
            case "resource" -> parseResourceCommand(tokens, operator, issuedFrom, dryRun, now);
            case "broadcast" -> parseBroadcastCommand(tokens, operator, issuedFrom, dryRun, now);
            default -> throw new ControlCommandValidationException("Unknown console command: " + tokens.getFirst() + ".");
        };
    }

    private ControlCommand parsePlayerCommand(ArrayList<String> tokens, ControlOperator operator, CommandIssuedFrom issuedFrom, boolean dryRun, Instant now) {
        requireSize(tokens, 3, "player debug <universalPlayerId>");
        if (!tokens.get(1).equalsIgnoreCase("debug")) {
            throw new ControlCommandValidationException("Expected player debug <universalPlayerId>.");
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
        if (!tokens.get(1).equalsIgnoreCase("healing")) {
            throw new ControlCommandValidationException("Expected tick healing [count].");
        }
        String count = tokens.size() >= 3 ? tokens.get(2) : "1";
        return command(ControlCommandType.RUN_HEALING_TICK, operator, issuedFrom, CommandTargetScope.GLOBAL, Set.of(), Map.of("tickCount", count), dryRun, now);
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
