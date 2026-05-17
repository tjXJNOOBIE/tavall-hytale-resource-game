package com.tavall.resourcegame.middleware.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class KdControlCommandTranslationHandler {
    private static final Set<String> KNOWN_KD_CATEGORIES = Set.of(
            "ui",
            "data",
            "castle",
            "interior",
            "citizens",
            "companion",
            "companions",
            "troops",
            "resources",
            "building",
            "buildings",
            "nodes",
            "trade",
            "market",
            "scout",
            "recon",
            "intel",
            "place",
            "focus",
            "interact",
            "scan",
            "account",
            "hologram",
            "holo",
            "entity",
            "entities",
            "bootstrap",
            "scene",
            "tick",
            "kingdom",
            "coord",
            "coordinate",
            "instance",
            "params",
            "parameter",
            "parameters",
            "clock",
            "schedule",
            "aging",
            "retaliation",
            "tutorial",
            "help"
    );

    public Optional<String> translateKdCommand(String normalizedInput) {
        return translateKdCommand(normalizedInput, "");
    }

    public Optional<String> translateKdCommand(String normalizedInput, String platformAccountId) {
        ArrayList<String> tokens = tokenize(normalizedInput);
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        String root = tokens.getFirst().toLowerCase(Locale.ROOT);
        if (!root.equals("kd") && !root.equals("kingdom")) {
            return Optional.of(normalizedInput);
        }
        if (tokens.size() < 2) {
            return Optional.empty();
        }
        String category = tokens.get(1).toLowerCase(Locale.ROOT);
        return switch (category) {
            case "tick" -> translateTick(tokens);
            case "kingdom" -> translatePrefixed(tokens, "kingdom");
            case "coord", "coordinate" -> translatePrefixed(tokens, "coord");
            case "instance" -> translatePrefixed(tokens, "instance");
            case "params", "parameter", "parameters" -> translatePrefixed(tokens, "params");
            case "clock" -> translatePrefixed(tokens, "clock");
            case "schedule" -> translatePrefixed(tokens, "schedule");
            case "aging" -> translatePrefixed(tokens, "aging");
            case "data" -> translateSelfDebug(tokens, platformAccountId);
            case "ui", "castle", "interior", "building", "buildings", "nodes", "place", "focus", "interact", "scan",
                    "hologram", "holo", "entity", "entities", "bootstrap", "scene", "tutorial",
                    "trade", "market", "scout", "recon", "intel", "retaliation", "help" -> translateFrontendKd(tokens, category);
            case "citizens" -> translateCitizens(tokens, platformAccountId);
            case "companion", "companions" -> translateCompanion(tokens, platformAccountId);
            case "resources" -> translateResources(tokens, platformAccountId);
            case "troops" -> translateTroops(tokens, platformAccountId);
            case "account" -> translateAccount(tokens, platformAccountId);
            default -> Optional.empty();
        };
    }

    public String category(String normalizedInput) {
        ArrayList<String> tokens = tokenize(normalizedInput);
        if (tokens.size() < 2) {
            return "root";
        }
        String category = tokens.get(1).toLowerCase(Locale.ROOT);
        return KNOWN_KD_CATEGORIES.contains(category) ? category : "unknown";
    }

    private Optional<String> translateTick(List<String> tokens) {
        if (tokens.size() == 2) {
            return Optional.of("tick healing 1");
        }
        if (tokens.get(2).equalsIgnoreCase("healing")) {
            String count = tokens.size() >= 4 ? tokens.get(3) : "1";
            return Optional.of("tick healing " + count);
        }
        if (tokens.get(2).equalsIgnoreCase("clock")) {
            if (tokens.size() >= 4 && tokens.get(3).equalsIgnoreCase("all")) {
                return Optional.of("tick clock all");
            }
            return Optional.of("tick clock " + (tokens.size() >= 4 ? tokens.get(3) : "kingdom-1"));
        }
        if (tokens.get(2).equalsIgnoreCase("run")) {
            String count = tokens.size() >= 4 ? tokens.get(3) : "1";
            return Optional.of("tick healing " + count);
        }
        return Optional.empty();
    }

    private Optional<String> translatePrefixed(List<String> tokens, String prefix) {
        if (tokens.size() < 3) {
            return Optional.empty();
        }
        return Optional.of(prefix + " " + String.join(" ", tokens.subList(2, tokens.size())));
    }

    private Optional<String> translateFrontendKd(List<String> tokens, String category) {
        return Optional.of("frontend kd " + normalizeLegacyCategory(category) + " " + String.join(" ", tokens));
    }

    private Optional<String> translateSelfDebug(List<String> tokens, String platformAccountId) {
        if (platformAccountId != null && !platformAccountId.isBlank()) {
            return Optional.of("player debug " + platformAccountId);
        }
        return translateFrontendKd(tokens, "data");
    }

    private Optional<String> translateResources(List<String> tokens, String platformAccountId) {
        if (tokens.size() >= 6 && (tokens.get(2).equalsIgnoreCase("give") || tokens.get(2).equalsIgnoreCase("grant"))) {
            return Optional.of("resource give " + tokens.get(3) + " " + tokens.get(4) + " " + tokens.get(5));
        }
        if (tokens.size() >= 5 && tokens.get(2).equalsIgnoreCase("add") && platformAccountId != null && !platformAccountId.isBlank()) {
            return Optional.of("resource give " + platformAccountId + " " + resourceAssetId(tokens.get(3)) + " " + tokens.get(4));
        }
        return translateFrontendKd(tokens, "resources");
    }

    private Optional<String> translateCitizens(List<String> tokens, String platformAccountId) {
        if (tokens.size() >= 3 && Set.of("spawn", "create", "migrate", "list", "summary", "debug", "get", "age", "ageall", "setstage", "setjob", "clearjob", "train", "promote", "demote", "health", "morale", "nutrition", "housing", "maintenance", "refresh-cache", "refresh-displays", "food-effects", "morale-effects", "night-rest").contains(tokens.get(2).toLowerCase(Locale.ROOT))) {
            return Optional.of("citizens " + String.join(" ", tokens.subList(2, tokens.size())));
        }
        if (tokens.size() >= 4 && tokens.get(2).equalsIgnoreCase("add") && platformAccountId != null && !platformAccountId.isBlank()) {
            return Optional.of("citizens spawn " + platformAccountId + " " + tokens.get(3) + " kingdom-1");
        }
        return translateFrontendKd(tokens, "citizens");
    }

    private Optional<String> translateCompanion(List<String> tokens, String platformAccountId) {
        if (tokens.size() < 3) {
            return Optional.empty();
        }
        String operation = tokens.get(2).toLowerCase(Locale.ROOT);
        if (operation.equals("list")) {
            if (tokens.size() >= 4) {
                return Optional.of("companion list " + tokens.get(3));
            }
            if (platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("companion list " + platformAccountId);
            }
            return Optional.empty();
        }
        if (operation.equals("give") || operation.equals("create")) {
            if (tokens.size() >= 5) {
                return Optional.of("companion give " + tokens.get(3) + " " + tokens.get(4));
            }
            if (tokens.size() >= 4 && platformAccountId != null && !platformAccountId.isBlank() && !looksLikeUuid(tokens.get(3))) {
                return Optional.of("companion give " + platformAccountId + " " + tokens.get(3));
            }
            return Optional.empty();
        }
        if (operation.equals("train") || operation.equals("claim") || operation.equals("cancel") || operation.equals("summon") || operation.equals("recall")) {
            if (tokens.size() >= 5) {
                return Optional.of("companion " + operation + " " + tokens.get(3) + " " + tokens.get(4));
            }
            if (tokens.size() >= 4 && platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("companion " + operation + " " + platformAccountId + " " + tokens.get(3));
            }
            return Optional.empty();
        }
        if (operation.equals("skill")) {
            if (tokens.size() >= 7) {
                return Optional.of("companion skill " + tokens.get(3) + " " + tokens.get(4) + " " + tokens.get(5) + " " + tokens.get(6));
            }
            if (tokens.size() >= 6 && platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("companion skill " + tokens.get(3) + " " + platformAccountId + " " + tokens.get(4) + " " + tokens.get(5));
            }
            return Optional.empty();
        }
        if (operation.equals("wall")) {
            if (tokens.size() >= 7 && tokens.get(3).equalsIgnoreCase("assign")) {
                return Optional.of("companion wall assign " + tokens.get(4) + " " + tokens.get(5) + " " + tokens.get(6));
            }
            if (tokens.size() >= 6 && tokens.get(3).equalsIgnoreCase("assign") && platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("companion wall assign " + platformAccountId + " " + tokens.get(4) + " " + tokens.get(5));
            }
            if (tokens.size() >= 6 && tokens.get(3).equalsIgnoreCase("remove")) {
                return Optional.of("companion wall remove " + tokens.get(4) + " " + tokens.get(5));
            }
            if (tokens.size() >= 5 && tokens.get(3).equalsIgnoreCase("remove") && platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("companion wall remove " + platformAccountId + " " + tokens.get(4));
            }
            if (tokens.size() >= 4 && tokens.get(3).equalsIgnoreCase("debug")) {
                if (tokens.size() >= 5) {
                    return Optional.of("companion wall debug " + tokens.get(4));
                }
                if (platformAccountId != null && !platformAccountId.isBlank()) {
                    return Optional.of("companion wall debug " + platformAccountId);
                }
                return Optional.empty();
            }
            return Optional.empty();
        }
        if (Set.of("debug", "get", "setlevel", "xp", "morale", "behavior", "projection", "ui").contains(operation)) {
            return Optional.of("companion " + String.join(" ", tokens.subList(2, tokens.size())));
        }
        return Optional.empty();
    }

    private Optional<String> translateTroops(List<String> tokens, String platformAccountId) {
        if (tokens.size() >= 4 && tokens.get(2).equalsIgnoreCase("debug")) {
            return Optional.of("troop debug " + tokens.get(3));
        }
        if (tokens.size() >= 6 && tokens.get(2).equalsIgnoreCase("wound")) {
            return Optional.of("troop wound " + tokens.get(3) + " " + tokens.get(4) + " " + tokens.get(5));
        }
        if (tokens.size() >= 6 && tokens.get(2).equalsIgnoreCase("heal")) {
            StringBuilder command = new StringBuilder("troop heal ")
                    .append(tokens.get(3))
                    .append(' ')
                    .append(tokens.get(4))
                    .append(' ')
                    .append(tokens.get(5));
            if (tokens.size() >= 7) {
                command.append(' ').append(tokens.get(6));
            }
            if (tokens.size() >= 8) {
                command.append(' ').append(tokens.get(7));
            }
            return Optional.of(command.toString());
        }
        return translateFrontendKd(tokens, "troops");
    }

    private Optional<String> translateAccount(List<String> tokens, String platformAccountId) {
        if (tokens.size() < 3) {
            if (platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("account status " + platformAccountId);
            }
            return Optional.empty();
        }
        String operation = tokens.get(2).toLowerCase(Locale.ROOT);
        if (operation.equals("status")) {
            if (tokens.size() >= 4) {
                return Optional.of("account status " + tokens.get(3));
            }
            if (platformAccountId != null && !platformAccountId.isBlank()) {
                return Optional.of("account status " + platformAccountId);
            }
            return Optional.empty();
        }
        if (operation.equals("addxp") || operation.equals("xp")) {
            if (tokens.size() < 4) {
                return Optional.empty();
            }
            String amount = tokens.get(3);
            String target = tokens.size() >= 5 ? tokens.get(4) : platformAccountId;
            if (target == null || target.isBlank()) {
                return Optional.empty();
            }
            return Optional.of("account addxp " + target + " " + amount);
        }
        if (operation.equals("setlevel") || operation.equals("level")) {
            if (tokens.size() < 4) {
                return Optional.empty();
            }
            String level = tokens.get(3);
            String target = tokens.size() >= 5 ? tokens.get(4) : platformAccountId;
            if (target == null || target.isBlank()) {
                return Optional.empty();
            }
            return Optional.of("account setlevel " + target + " " + level);
        }
        if (operation.equals("debug")) {
            if (tokens.size() < 4) {
                return Optional.empty();
            }
            String mode = tokens.get(3);
            String target = tokens.size() >= 5 ? tokens.get(4) : platformAccountId;
            if (target == null || target.isBlank()) {
                return Optional.empty();
            }
            return Optional.of("account debug " + target + " " + mode);
        }
        return translateFrontendKd(tokens, "account");
    }

    private boolean looksLikeUuid(String value) {
        if (value == null) {
            return false;
        }
        try {
            java.util.UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String normalizeLegacyCategory(String category) {
        return switch (category.toLowerCase(Locale.ROOT)) {
            case "holo" -> "hologram";
            case "entities" -> "entity";
            default -> category.toLowerCase(Locale.ROOT);
        };
    }

    private String resourceAssetId(String resourceType) {
        return switch (resourceType.toLowerCase(Locale.ROOT)) {
            case "food" -> "resource.food";
            case "wood" -> "resource.wood";
            case "iron" -> "resource.iron";
            default -> resourceType;
        };
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
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
